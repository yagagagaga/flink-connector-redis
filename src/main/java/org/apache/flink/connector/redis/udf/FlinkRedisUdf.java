package org.apache.flink.connector.redis.udf;

import org.apache.flink.connector.redis.client.RedisClientProxy;
import org.apache.flink.connector.redis.options.RedisOptions;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * FlinkRedisUdf.
 */
public final class FlinkRedisUdf {

	private static String generateCode(String className, String name, List<Method> methods) {
		String template = "package org.apache.flink.connector.redis.udf;\n" +
				"\n" +
				"import org.apache.flink.connector.redis.options.RedisOptions;\n" +
				"\n" +
				"/**\n" +
				" * %s.\n" +
				" */\n" +
				"public class %s extends RedisScalarFunction {\n" +
				"\n" +
				"\tpublic %s(RedisOptions options) {\n" +
				"\t\tsuper(options);\n" +
				"\t}\n" +
				"\n" +
				"%s" +
				"}\n";
		StringBuilder methodsStr = new StringBuilder();
		for (Method method : methods) {
			final Class<?>[] parameterTypes = method.getParameterTypes();

			boolean skip = false;
			String[] parameterStr = new String[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				final String typeName = parameterTypes[i].getTypeName();
				if (typeName.startsWith("redis.")) {
					skip = true;
					break;
				}
				parameterStr[i] = typeName + " var" + i;
			}
			if (skip) {
				continue;
			}

			String m = ""
					+ "\tpublic " + method.getReturnType().getTypeName() + " eval(" + String.join(",", parameterStr) + ") {\n"
					+ "\t\treturn client." + name + "(" + IntStream.range(0, parameterTypes.length).mapToObj(x -> "var" + x).collect(Collectors.joining(",")) + ");\n"
					+ "\t}\n\n";
			methodsStr.append(m);
		}
		return String.format(template, className, className, className, methodsStr);
	}

	public static void registerTo(StreamTableEnvironment tableEnv, RedisOptions options) {
		tableEnv.createTemporaryFunction("redis_get", new RedisGetFunction(options));
		tableEnv.createTemporaryFunction("redis_set", new RedisSetFunction(options));
		tableEnv.createTemporaryFunction("redis_hget", new RedisHgetFunction(options));
		tableEnv.createTemporaryFunction("redis_hset", new RedisHsetFunction(options));
	}

	public static void main(String[] args) throws Exception {
		final Method[] methods = RedisClientProxy.class.getMethods();
		final Map<String, List<Method>> collect = Arrays.stream(methods).collect(Collectors.groupingBy(Method::getName));

		final HashSet<String> set = new HashSet<>(Arrays.asList("get", "set", "hget", "hset"));
		for (Map.Entry<String, List<Method>> entry : collect.entrySet()) {
			final String methodName = entry.getKey();
			if (set.contains(methodName)) {
				String className = "Redis"
						+ methodName.substring(0, 1).toUpperCase()
						+ methodName.substring(1)
						+ "Function";
				final String javaCode = generateCode(className, methodName, entry.getValue());
				final String target = Objects.requireNonNull(
						FlinkRedisUdf.class.getClassLoader().getResource("")).getPath();
				final String packagePath = target.substring(0, target.indexOf("target"))
						+ "src/main/java/"
						+ FlinkRedisUdf.class.getPackage().getName().replace(".", "/");
				FileUtils.write(new File(packagePath + "/" + className + ".java"), javaCode, "utf8");
			}
		}
	}
}
