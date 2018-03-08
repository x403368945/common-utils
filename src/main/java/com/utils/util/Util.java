package com.utils.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.utils.common.entity.base.ItemInfo;
import com.utils.enums.Code;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.MessageDigest;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 系统工具类
 *
 * @author Jason Xie 2016-11-23
 *
 */
@Slf4j
public class Util {
	/**
	 * 获取UUID
	 *
	 * @return String
	 */
	public static String uuid() {
		return UUID.randomUUID().toString();
	}
	/**
	 * 获取指定长度的随机数字字符串，长度不够补零
	 * @param length int 长度
	 * @return String
	 */
	public static String random(int length){
		return RandomStringUtils.randomNumeric(length);
	}
	/**
	 * 获取随机数，指定随机数最大值
	 * @param max int
	 * @return int
	 */
	public static int randomMax(int max){
		return new Random().nextInt(max);
	}
	/**
	 * 转换为Boolean类型，返回值可以为null
	 *
	 * @param obj 转换对象
	 * @return Boolean
	 */
	public static Boolean toBoolean(Object obj) {
		try {
			return isEmpty(obj) ? null : Boolean.valueOf(obj.toString());
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 转换为boolean类型，返回值不能为null
	 *
	 * @param obj 转换对象
	 * @return boolean
	 */
	public static boolean booleanValue(Object obj) {
		Boolean value = toBoolean(obj);
		return isNotEmpty(value) && value;
	}
	/**
	 * 将Object 转换为String，""转换为null
	 *
	 * @param obj 转换对象
	 * @return String
	 */
	public static String tostring(Object obj) {
		if (Objects.isNull(obj)) return null;
		return "".equals(obj.toString().trim()) ? null : obj.toString().trim();
	}
	/**
	 * 将Object 转换为String，null转换为""，且去掉左右空格
	 *
	 * @param obj 转换对象
	 * @return String
	 */
	public static String toempty(Object obj) {
		return (Objects.isNull(obj)) ? "" : obj.toString().trim();
	}
	/**
	 * 验证数字是否大于0
	 *
	 * @param value Number对象
	 * @return true大于0，false小于等于0
	 */
	public static boolean checkNumber(Number value) {
		return Objects.nonNull(value) && value.doubleValue() > 0;
	}
	/**
	 * 判断Boolean值是否为true，为null时表示false
	 * @param value Boolean
	 * @return boolean true：非空且值为true，false为空或值为false
	 */
	public static boolean isTrue(Boolean value) {
		return Objects.nonNull(value) && value;
	}
	/**
	 * 判断Boolean值是否为false，为null时表示false
	 * @param value Boolean
	 * @return boolean true：为空或者值为false，false为空且值为true
	 */
	public static boolean isFalse(Boolean value) {
		return !isTrue(value);
	}
	/**
	 * 计算分页总页数
	 *
	 * @param pageSize 每页数量
	 * @param count 总行数
	 * @return int 总页数
	 */
	public static int getPageCount(int pageSize, int count) {
		if (checkNumber(count))
			return (count % pageSize == 0) ? (count / pageSize) : (count / pageSize + 1);
		else
			return 1;
	}
	/**
	 * MD5加密
	 *
	 * @param source 加密字符串
	 * @return String 密文字符串
	 */
	public static String md5(String source) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(source.getBytes("UTF8"));
			byte[] bytes = md5.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
			}
//			for (int i = 0; i < bytes.length; ++i) {
//				sb.append(Integer.toHexString((bytes[i] & 0xFF) | 0x100).substring(1, 3));
//			}
			return sb.toString();
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return null;
	}
	/**
	 * 判断列表是否为空
	 * @param collections 集合
	 * @return boolean true空 false非空
	 */
	public static boolean isEmpty(Collection<?> collections) {
		return Objects.isNull(collections) || collections.isEmpty();
	}
	/**
	 * 判断列表是否非空
	 * @param collections 集合
	 * @return boolean true非空 false空
	 */
	public static boolean isNotEmpty(Collection<?> collections){
		return !isEmpty(collections);
	}
	/**
	 * 判断Map列表是否为空
	 * @param map 集合
	 * @return boolean true空 false非空
	 */
	public static boolean isEmpty(Map<?,?> map){
		return Objects.isNull(map) || map.isEmpty();
	}
	/**
	 * 判断Map列表是否非空
	 * @param map 集合
	 * @return boolean true非空 false空
	 */
	public static boolean isNotEmpty(Map<?,?> map){
		return !isEmpty(map);
	}
	/**
	 * 判断数组是否为空
	 * @param array T[] 数组
	 * @return boolean true空 false非空
	 */
	public static <T> boolean isEmpty(T[] array){
		return Objects.isNull(array) || array.length == 0;
	}
	/**
	 * 判断数组是否非空
	 * @param array T[] 数组
	 * @return boolean true非空 false空
	 */
	public static <T> boolean isNotEmpty(T[] array){
		return !isEmpty(array);
	}
	/**
	 * 判断对象是否为空
	 * @param obj Object 对象
	 * @return boolean true空 false非空
	 */
	public static boolean isEmpty(Object obj){
		return Objects.isNull(obj);
	}
	/**
	 * 判断对象是否为非空
	 * @param obj Object 对象
	 * @return boolean true非空 false空
	 */
	public static boolean isNotEmpty(Object obj){
		return !isEmpty(obj);
	}
	/**
	 * 判断字符串是否为空
	 * @param obj String 对象
	 * @return boolean true空 false非空
	 */
	public static boolean isEmpty(String obj){
		return Objects.isNull(obj) || "".equals(obj.trim());
	}
	/**
	 * 判断字符串是否为非空
	 * @param obj String 对象
	 * @return boolean true非空 false空
	 */
	public static boolean isNotEmpty(String obj){
		return !isEmpty(obj);
	}
	/**
	 * 将Object数组转换为String数组
	 * @param array Object[]
	 * @return String[]
	 */
	public static String[] toStringArray(Object[] array) {
		return isEmpty(array)
				? new String[]{}
				: Arrays.stream(array).map(Object::toString).toArray(String[]::new);
	}
	/**
	 * 判断args是否包含value
	 * @param value Object
	 * @param args Object...
	 * @return boolean true：args中包含value， false：args不包含value
	 */
	public static boolean in(Object value, Object... args) {
		return Objects.nonNull(value) && Objects.nonNull(args) && Arrays.asList(args).contains(value);
	}
	/**
	 * 判断args是否包含value
	 * @param value Object
	 * @param args Object...
	 * @return boolean true：args不包含value， false：args中包含value
	 */
	public static boolean notin(Object value, Object... args) {
		return !in(value, args);
	}
	/**
	 * 判断args是否包含value
	 * @param value Object
	 * @param collection Collection
	 * @return boolean true：args中包含value， false：args不包含value
	 */
	public static boolean in(Object value, Collection collection) {
		return Objects.nonNull(value) && Objects.nonNull(collection) && collection.contains(value);
	}
	/**
	 * 判断args是否包含value
	 * @param value
	 * @param collection
	 * @return boolean true：args不包含value， false：args中包含value
	 */
	public static boolean notin(Object value, Collection collection) {
		return !in(value, collection);
	}
	/**
	 * 对数组进行排序，不改变原数组序列
	 * @param args String[]
	 * @return String[]
	 */
	public static String[] sort(String... args) {
		return sort(true, args);
	}
	/**
	 * 对数组进行排序，不改变原数组序列
	 * @param asc boolean true:正序排列，false：倒序排列
	 * @param args String[]
	 * @return String[]
	 */
	public static String[] sort(boolean asc, String... args) {
		if (isEmpty(args)) return null;
		String[] arrays = args.clone();
		if (asc) Arrays.sort(arrays);
		else Arrays.sort(arrays, Collections.reverseOrder());
		return arrays;
	}
	/**
	 * 合并多个数组
	 * @param args String[]
	 * @return String[]
	 */
	public static String[] concat(String[] ...args) {
		return Arrays.stream(args).flatMap(Arrays::stream).toArray(String[]::new);
	}
	/**
	 * 合并多个数组
	 * @param args Object[]
	 * @return Object[]
	 */
	public static Object[] concat(Object[] ...args){
		return Arrays.stream(args).flatMap(Arrays::stream).toArray();
	}
	/**
	 * 合并map
	 * @param dest Map[]
	 * @param sources Map
	 */
	public static void assign(Map<String, Object> dest, Map<String, Object>... sources) {
		for (Map<String, Object> map : sources)
			dest.putAll(map);
	}
	/**
	 * 合并对象（sources）到目标对象（dest）
	 * @param dest JSONObject 目标对象
	 * @param sources JSONObject[]
	 */
	public static void assign(JSONObject dest, JSONObject... sources) {
		for (JSONObject obj : sources)
			dest.putAll(obj);
	}
	/**
	 * 将首字母变成小写
	 * @param text String 处理字符串
	 */
	public static String firstLowerCase(String text) {
		return text.replaceFirst("^[A-Z]", (String.valueOf(text.charAt(0))).toLowerCase());
	}
	/**
	 * obj.toString之后打印日志，并返回原对象
	 * @param obj <T>
	 * @return obj
	 */
	public static <T> T peek(T obj) {
		log.debug(isEmpty(obj) ? "obj is null" : Objects.toString(obj));
		return obj;
	}
	/**
	 * 打印日志，并返回原对象；可以自定义日志输出
	 * @param obj <T>
	 * @param consumer Consumer<T>
	 * @return obj
	 */
	public static <T> T peek(T obj, Consumer<T> consumer) {
		if (Objects.isNull(consumer))
			log.debug(isEmpty(obj) ? "obj is null" : Objects.toString(obj));
		else consumer.accept(obj);
		return obj;
	}
	/**
	 * 将对象格式化成json字符串打印日志，并返回原对象
	 * @param obj <T>
	 * @return obj
	 */
	public static <T> T peekJson(T obj){
		log.debug(isEmpty(obj) ? "obj is null" : JSON.toJSONString(obj, SerializerFeature.PrettyFormat));
		return obj;
	}

	/**
	 * 转换枚举
	 * @param elementType Class<E> 枚举类
	 * @param name String 枚举名
	 * @return Optional<E>
	 */
	public static <E extends Enum<E>> Optional<E> enumOf(Class<E> elementType, final String name) {
		try {
			return Optional.of(Enum.valueOf(elementType, name));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Optional.empty();
		}
	}
	/**
	 * 切割list
	 *
	 * @param list List 需要切割的集合
	 * @param size int 每个集合的大小
	 * @return List<List<T>>
	 */
	public static <T> List<List<T>> partition(final List<T> list, final int size) {
		final int max = list.size();
		return Stream.iterate(0, n -> n + 1)
				.limit(max / size + Math.min(1, max % size))
				.map(n -> list.subList(n * size, Math.min(max, (n + 1) * size)))
//				.map(n -> list.stream().skip(n * size).limit(size).collect(Collectors.toList()))
				.collect(Collectors.toList());
	}

	public static void main(String[] args) {
        log.debug("RandomStringUtils.random : {}",
                JSON.toJSONString(
                        Stream.iterate(0, n -> n + 1).limit(10)
                                .map(n -> RandomStringUtils.random(6, "qwertyuiopasdfghjklzxcvbnm123456789"))
                                .toArray()
                        , SerializerFeature.PrettyFormat
                )
        );
        log.debug("RandomStringUtils.randomNumeric : {}",
                JSON.toJSONString(
                        Stream.iterate(0, n -> n + 1).limit(10)
                                .map(n -> RandomStringUtils.randomNumeric(6))
                                .toArray()
                        , SerializerFeature.PrettyFormat
                )
        );
        log.debug("RandomStringUtils.randomAlphabetic : {}",
                JSON.toJSONString(
                        Stream.iterate(0, n -> n + 1).limit(10)
                                .map(n -> RandomStringUtils.randomAlphabetic(6))
                                .toArray()
                        , SerializerFeature.PrettyFormat
                )
        );
        log.debug("RandomStringUtils.randomAlphanumeric : {}",
                JSON.toJSONString(
                        Stream.iterate(0, n -> n + 1).limit(10)
                                .map(n -> RandomStringUtils.randomAlphanumeric(6))
                                .toArray()
                        , SerializerFeature.PrettyFormat
                )
        );
        log.debug("RandomStringUtils.randomAscii : {}",
                JSON.toJSONString(
                        Stream.iterate(0, n -> n + 1).limit(10)
                                .map(n -> RandomStringUtils.randomAscii(6))
                                .toArray()
                        , SerializerFeature.PrettyFormat
                )
        );
        log.debug(uuid());
//		log.debug("{}", Code.SUCCESS);
//		log.debug("{}", in(Code.NO_PERMISSION, Code.SUCCESS, Code.FAILURE));
//		log.debug("{}", in(Code.SUCCESS, Code.SUCCESS, Code.FAILURE));
//		{ // 用户密码加密
//			log.debug("superadmin:"+new BCryptPasswordEncoder().encode("superadmin"));
//			log.debug("888888:"+new BCryptPasswordEncoder().encode("888888"));
//			log.debug("111111:"+new BCryptPasswordEncoder().encode("111111"));
//			// 修改密码,判断原密码输入是否正确
//			log.debug("{}", new BCryptPasswordEncoder().matches("superadmin", "$2a$10$b9v.0glE7vYsrP9z.VMtV.ZRmBn05B1RgU3vFEjJ0O/E2wP7mjB8u"));
//		}
		log.debug(JSON.toJSONString(Arrays.asList(ItemInfo.builder().label("标题1").value("内容1").build(), ItemInfo.builder().label("标题2").value("内容2").build())));
		log.debug(JSON.toJSONString(concat(new String[]{"x","y","Z"}, new String[]{"a","B","c"}, new String[]{"SD","E","f"})));
		log.debug(JSON.toJSONString(
				concat(
						new ItemInfo[]{
								ItemInfo.builder().label("标题1").value("内容1").build(),
								ItemInfo.builder().label("标题2").value("内容2").build()
						}, new ItemInfo[]{
								ItemInfo.builder().label("标题3").value("内容3").build(),
								ItemInfo.builder().label("标题4").value("内容4").build()
						}
				)
		));
		{
			log.debug("{}", in(1, 1,2,3));
			log.debug("{}", in(1, 2,3));
			log.debug("{}", in(new Integer(1), 1,new Integer(2),3));
			log.debug("{}", in(new Integer(1), 2,3));
			log.debug("{}", in(new Double(1.0), new Double(1),new Integer(2),3));
			log.debug("{}", in(new Double(1), 2,3));
			log.debug("{}", in("1", "1","21","3"));
			log.debug("{}", in("1", "21","3"));
			log.debug("{}", in("1", Arrays.asList("1","21","3")));
			log.debug("{}", in("2", Arrays.asList("1","21","3")));
		}
		log.debug("{}",Util.enumOf(Code.class, "SUCCESS").orElse(null));
//		log.debug("{}",Util.enumOf(Code.class, "Success").orElse(null));
	}
}
