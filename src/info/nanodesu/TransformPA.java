package info.nanodesu;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class TransformPA {
	public static void main(String[] args) throws NumberFormatException,
			IOException {
		if (args.length != 2) {
			System.out
					.println("Usage: TransformPA <path to configuration> <path to transform.json>. Trying to use my hardcoded path because I am lazy, this will crash for you if you are not Cola_Colin...");
			args = new String[] {
					"E:\\Dev\\Eclipse\\scalaide\\workspace\\TransformPAX\\conf.json",
					"E:\\Dev\\Eclipse\\scalaide\\workspace\\TransformPAX\\transform.json" };
		}

		JsonObject conf = JsonObject.readFrom(FileUtils
				.readFileToString(new File(args[0])));
		String media = conf.get("media").asString();
		String output = conf.get("output").asString();
		JsonValue jsonValue = conf.get("modInfo");
		jsonValue.asObject().set("date", new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
		String modInfo = jsonValue.toString();
		
		JsonArray transforms = JsonArray.readFrom(FileUtils
				.readFileToString(new File(args[1])));

		new TransformPA(modInfo, media, output, transforms).createMod();
	}

	private File basePath;
	private File outputPath;
	private JsonArray transforms;
	private String modInfo;
	
	private Set<String> processedUnits = new HashSet<>();
	
	public TransformPA(String modInfo, String media, String out, JsonArray transforms) {
		this.modInfo = modInfo;
		basePath = new File(media);
		outputPath = new File(out);
		this.transforms = transforms;
	}

	public void createMod() throws IOException {
		FileUtils.writeStringToFile(new File(outputPath, "modinfo.json"), modInfo);

		for (JsonValue transform : transforms.values()) {
			JsonArray units = transform.asObject().get("affects").asArray();
			JsonArray ops = transform.asObject().get("ops").asArray();
			for (JsonValue unit : units) {
				processUnit(unit.asString(), ops);
			}
		}
	}

	private void processUnit(String unit, JsonArray ops) {
		try {
			System.out.println("========== processing " + unit);
			File src = processedUnits.contains(unit) ? new File(outputPath, unit) : new File(basePath, unit);
			String jsonStr = FileUtils.readFileToString(src);
			File outJson = new File(outputPath, unit);
			outJson.getParentFile().mkdirs();
			FileUtils.writeStringToFile(outJson, processUnitJson(jsonStr, ops));
			System.out.println("========== processed " + unit);
			processedUnits.add(unit);
		} catch (Exception ex) {
			System.out.println("Error processing unit: " + unit);
			ex.printStackTrace(System.out);
		}
	}

	private String processUnitJson(String str, JsonArray ops)
			throws IOException {
		JsonObject obj = JsonObject.readFrom(str);

		for (JsonValue opV : ops.values()) {
			JsonObject op = opV.asObject();
			for (JsonValue keyV : op.get("keys").asArray().values()) {
				modifyValue(obj, op, keyV);
			}
		}

		return obj.toString();
	}

	private void modifyValue(JsonObject obj, JsonObject op, JsonValue keyV) {
		List<JsonValue> keyPath = new ArrayList<>(keyV.asArray().values());
		JsonObject target = obj;
		String fullPath = keyPath.toString();
		while (keyPath.size() > 1) {
			String selectKey = keyPath.remove(0).asString();
			target = target.get(selectKey).asObject();
		}
		String finalKey = keyPath.get(0).asString();
		System.out.println("modify key "+fullPath);
		String cmd = op.get("cmd").asString();
		double value = op.get("value").asDouble();
		
		double result;
		
		if (target.get(finalKey) != null) {
			double src = target.get(finalKey).asDouble();

			switch (cmd) {
			case "=":
				result = value;
				break;
			case "*":
				result = src * value;
				break;
			case "/":
				result = src / value;
				break;
			case "+":
				result = src + value;
				break;
			case "-":
				result = src - value;
				break;
			default:
				throw new RuntimeException("unknown cmd in op: " + cmd);
			}
		} else {
			if ("=".equals(cmd)) {
				result = value;
			} else {
				throw new RuntimeException("cmd is "+cmd+" but the key "+ finalKey+ " does not exit!");
			}
		}

		target.set(finalKey, result);
	}
}
