package model;

import com.utils.util.Dates;
import com.utils.util.Util;
import enums.DIR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jason Xie on 2018/3/15.
 */
public class DB2JavaClass {
    public static void main(String[] args) {
        writeTemplate();
        try {
            final StringBuilder sb = new StringBuilder();
            Files.readAllLines(Paths.get("src/main/resources/db/db.sql").toAbsolutePath())
                    .stream()
                    .map(String::trim)
                    .filter(Util::isNotEmpty)
//                    .peek(System.out::println)
                    .forEach(line->{
                        if (line.startsWith("CREATE TABLE")) { // 开始
                            Config.init(line.replace("CREATE TABLE", "").replace("(","").trim());
                            sb.delete(0, sb.length());
                        } else if (line.startsWith("`")) { // 字段
                            { // 注释
                                final Matcher m = Pattern.compile("(?<=').+(?=')").matcher(line);
                                sb.append("\t/**\n\t * ");
                                if (m.find()) sb.append(m.group());
                                sb.append("\n\t */\n");
                            }
                            if (line.startsWith("`id`")) {
                                sb.append("\t@Id\n");
                                if(line.toUpperCase().contains(" BIGINT")) sb.append("\t@GeneratedValue(strategy = GenerationType.IDENTITY)\n\tprivate Long id;\n");
                                else sb.append("\t@GeneratedValue(generator = \"uuid2\")\n\t@GenericGenerator(name = \"uuid2\", strategy = \"uuid2\")\n\tprivate String id;\n");
                            } else if (line.startsWith("`createTime`"))
                                sb.append("\t@JSONField(deserialize = false, format = \"yyyy-MM-dd HH:mm:ss\")\n\t@Column(insertable = false, updatable = false)\n\tprivate Timestamp createTime;\n");
                            else if (line.startsWith("`createUserId`"))
                                sb.append("\t@JSONField(deserialize = false)\n\t@Column(updatable = false)\n\tprivate String createUserId;\n");
                            else if (line.startsWith("`modifyTime`"))
                                sb.append("\t@JSONField(deserialize = false, format = \"yyyy-MM-dd HH:mm:ss\")\n\t@Column(insertable = false, updatable = false)\n\tprivate Timestamp modifyTime;\n");
                            else if (line.startsWith("`modifyUserId`"))
                                sb.append("\t@JSONField(deserialize = false)\n\t@Column(updatable = false)\n\tprivate String modifyUserId;\n");
                            else if (line.startsWith("`deleted`"))
                                sb.append("\t@Column(insertable = false, updatable = false)\n\tprivate Switch deleted;\n");
                            else {
                                final Matcher m = Pattern.compile("(?<=`).+(?=`)").matcher(line);
                                if(m.find()){
                                    if (line.toUpperCase().contains(" BIGINT")) sb.append("\tprivate Long ".concat(m.group()).concat(";\n"));
                                    else if (line.toUpperCase().contains(" INT")) sb.append("\tprivate Integer ".concat(m.group()).concat(";\n"));
                                    else if (line.toUpperCase().contains(" TINYINT")) sb.append("\tprivate Short ".concat(m.group()).concat(";\n"));
                                    else if (line.toUpperCase().contains(" DECIMAL")) sb.append("\tprivate Double ".concat(m.group()).concat(";\n"));
                                    else if (line.toUpperCase().contains(" VARCHAR")) sb.append("\tprivate String ".concat(m.group()).concat(";\n"));
                                    else if (line.toUpperCase().contains(" TEXT")) sb.append("\tprivate String ".concat(m.group()).concat(";\n"));
                                    else if (line.toUpperCase().contains(" TIMESTAMP")) sb.append("\t@JSONField(format = \"yyyy-MM-dd HH:mm:ss\")\n\tprivate Timestamp ".concat(m.group()).concat(";\n"));
                                    else if (line.toUpperCase().contains(" DATETIME")) sb.append("\t@JSONField(format = \"yyyy-MM-dd HH:mm:ss\")\n\tprivate Timestamp ".concat(m.group()).concat(";\n"));
                                    else if (line.toUpperCase().contains(" DATE")) sb.append("\t@JSONField(format = \"yyyy-MM-dd\")\n\tprivate Timestamp ".concat(m.group()).concat(";\n"));
                                    else System.err.println(line.concat("\n未找到匹配的数据类型"));
                                } else {
                                    System.err.println(line.concat("\n正则未匹配到属性名"));
                                }
                            }
                        } else if (line.startsWith(")")) { // 结束
                            try {
                                DIR.TEMP.write( // Tab.java
                                        Config.format(DIR.TEMP.read("Tab.java"))
                                                .replace("{IUser}", (sb.indexOf("private String createUserId;") > 0 || sb.indexOf("private String modifyUserId;") > 0) ? "IUser," : "")
                                                .replace("{props}", sb.toString())
                                        ,
                                        Config.javaname, "entity", Config.TabName.concat(".java")
                                );
                                DIR.TEMP.write( // Repository.java
                                        Config.format(DIR.TEMP.read("Repository.java"))
                                                .replace("{ID}", sb.indexOf("GenerationType.IDENTITY") > 0 ? "Long" : "String")
                                        ,
                                        Config.javaname, "dao", "jpa", Config.JavaName.concat("Repository.java")
                                );
                                DIR.TEMP.write( // Service.java
                                        Config.format(DIR.TEMP.read("Service.java"))
                                                .replace("{ID}", sb.indexOf("GenerationType.IDENTITY") > 0 ? "Long" : "String")
                                        ,
                                        Config.javaname, "service", Config.JavaName.concat("Service.java")
                                );
                                DIR.TEMP.write( // Controller.java
                                        Config.format(DIR.TEMP.read("Controller.java"))
                                                .replace("{ID}", sb.indexOf("GenerationType.IDENTITY") > 0 ? "Long" : "String")
                                        ,
                                        Config.javaname, "web", Config.JavaName.concat("Controller.java")
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.err.println(Config.tab_name.concat("文件写入失败"));
                            }
                            Config.clear();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**`
     * 写入模板
     */
    private static void writeTemplate() {
        try {
//            System.out.println(
//                    Files.readAllLines(DIR.TEMP.file("temp.java").toPath()).stream().map(l -> "\"" + l.replace("\"", "\\\"") + "\\n\"").collect(Collectors.joining("+"))
//            );
            DIR.TEMP.write(
                    "package com.business.{javaname}.web;\n/**\n * 请求操作响应：\n *\n * @author Jason Xie on {date}.\n */\n@RequestMapping(\"/{java_name}\")\n@Controller\n@Slf4j\npublic class {JavaName}Controller extends BaseContoller implements IController<{TabName}, {ID}> {\n	@Autowired\n	private {JavaName}Service service;\n//	/**\n//     * 保存\n//     * URL:/{模块url前缀}\n//     * 参数：params=JSONObject\n//     * @param paramsInfo ParamsInfo 参数对象\n//     * @return ResultInfo<E>\n//     */\n//    @PostMapping\n//    @ResponseBody\n//    @Override\n//    public ResultInfo<{TabName}> save(@RequestBody(required = false) ParamsInfo paramsInfo) {\n//        final ResultInfo<{TabName}> resultInfo = new ResultInfo<>();\n//        try {\n//            Params.notEmpty(paramsInfo, \"参数集合为空\");\n//            resultInfo.setSuccess(service.save(paramsInfo.parseObject({TabName}.class), getUserId()));\n//        } catch (Exception e) {\n//            log.error(e.getMessage(), e);\n//            resultInfo.setCode(e);\n//        }\n//        return resultInfo;\n//    }\n//	/**\n//     * 修改数据\n//     * URL:/{模块url前缀}/{id}\n//     * 参数：params=JSONObject\n//     * @param id String 数据ID\n//     * @param paramsInfo ParamsInfo 参数对象\n//     * @return ResultInfo<E>\n//     */\n//    @PutMapping(\"/{id}\")\n//    @ResponseBody\n//    @Override\n//    public ResultInfo<{TabName}> update(@PathVariable final {ID} id, @RequestBody(required = false) ParamsInfo paramsInfo) {\n//        final ResultInfo<{TabName}> resultInfo = new ResultInfo<>();\n//        try {\n//            Params.notEmpty(paramsInfo, \"参数集合为空\");\n//            service.update(id, getUserId(), paramsInfo.parseObject({TabName}.class));\n//            resultInfo.setCode(SUCCESS);\n//        } catch (Exception e) {\n//            log.error(e.getMessage(), e);\n//            resultInfo.setCode(e);\n//        }\n//        return resultInfo;\n//    }\n//	/**\n//     * 按ID删除\n//     * URL:/{模块url前缀}/{id}\n//     * 参数：{id}数据ID；\n//     *\n//     * @param id String 数据ID\n//     * @return ResultInfo<Object>\n//     */\n//    @DeleteMapping(\"/{id}\")\n//    @ResponseBody\n//    @Override\n//    public ResultInfo<Object> deleteById(@PathVariable final {ID} id) {\n//        return super.deleteById(service, id);\n//    }\n//	/**\n//     * 按ID删除，标记删除\n//     * URL:/{模块url前缀}/{id}\n//     * 参数：{id}数据ID；\n//     * @param id String 数据ID\n//     * @return ResultInfo<Object>\n//     */\n//    @PatchMapping(\"/{id}\")\n//    @ResponseBody\n//    @Override\n//    public ResultInfo<Object> markDeleteById(@PathVariable final {ID} id) {\n//        return super.markDeleteById(service, id);\n//    }\n//	/**\n//     * 批量操作按ID删除，逻辑删除\n//     * URL:/{模块url前缀}\n//     * 参数：params=JSONObject\n//     * @param paramsInfo ParamsInfo 参数对象\n//     * @return ResultInfo<Object>\n//     */\n//    @PatchMapping\n//    @ResponseBody\n//    @Override\n//    public ResultInfo<Object> markDeleteByIds(@RequestBody(required = false) ParamsInfo paramsInfo) {\n//        return super.markDeleteById(service, paramsInfo);\n//    }\n//	/**\n//     * 按ID查询\n//     * URL:/{模块url前缀}/{id}\n//     * 参数：{id}数据ID；\n//     * @param id 数据ID\n//     * @return ResultInfo<E>\n//     */\n//    @GetMapping(\"/{id}\")\n//    @ResponseBody\n//    @Override\n//    public ResultInfo<{TabName}> getById(@PathVariable final {ID} id) {\n//        final ResultInfo<{TabName}> resultInfo = new ResultInfo<>();\n//        try {\n//            resultInfo.setSuccess(service.getById(id).orElse(null));\n//        } catch (Exception e) {\n//            log.error(e.getMessage(), e);\n//            resultInfo.setCode(e);\n//        }\n//        return resultInfo;\n//    }\n//	/**\n//     * 按条件查询列表，不分页\n//     * URL:/{模块url前缀}\n//     * 参数：params=JSONObject\n//     * @param paramsInfo ParamsInfo 参数对象\n//     * @return ResultInfo<E>\n//     */\n//    @GetMapping\n//    @ResponseBody\n//    @Override\n//    public ResultInfo<{TabName}> search(@RequestParam(name = \"params\", required = false, defaultValue = \"{}\") final ParamsInfo paramsInfo) {\n//        final ResultInfo<{TabName}> resultInfo = new ResultInfo<>();\n//        try {\n//            final {TabName} condition = paramsInfo.required(false) // false 可以指定查询参数不是必须的，如果不指定false，则会校验参数是必须的，否则抛出异常\n//                    .parseObject({TabName}.class);\n//            // Params.notEmpty(condition.getCreateUserId(), \"参数【createUserId】不能为空\");\n//            // condition.setDeleted(Switch.NO); // 限定强制参数\n//            resultInfo.setSuccess(\n//                    service.findList(\n//                            condition,\n//                            {TabName}.Order.createTime.desc\n//                    )\n//            );\n//        } catch (Exception e) {\n//            log.error(e.getMessage(), e);\n//            resultInfo.setCode(e);\n//        }\n//        return resultInfo;\n//    }\n//	 /**\n//     * 按条件分页查询列表\n//     * URL:/{模块url前缀}/{pageIndex}/{pageSize}\n//     * 参数：{pageIndex}当前页索引；{pageSize}每页大小；params=JSONObject\n//     * @param paramsInfo ParamsInfo 参数对象\n//     * @return ResultInfo<{TabName}>\n//     */\n//    @GetMapping(\"/{pageIndex}/{pageSize}\")\n//    @ResponseBody\n//    @Override\n//    public ResultInfo<{TabName}> search(\n//			@PathVariable final int pageIndex, \n//			@PathVariable final int pageSize, \n//			@RequestParam(name = \"params\", required = false, defaultValue = \"{}\") final ParamsInfo paramsInfo) {\n//        final ResultInfo<{TabName}> resultInfo = new ResultInfo<>();\n//        try {\n//            final {TabName} condition = paramsInfo.required(false) // false 可以指定查询参数不是必须的，如果不指定false，则会校验参数是必须的，否则抛出异常\n//                    .parseObject({TabName}.class);\n//            // Params.notEmpty(condition.getCreateUserId(), \"参数【createUserId】不能为空\");\n//            // condition.setDeleted(Switch.YES); // 限定强制参数\n//            resultInfo.setSuccess(\n//				service.findPage(\n//                    condition,\n//                    Pager.builder()\n//                            .index(pageIndex)\n//                            .size(pageSize)\n//                            .build()\n//                            .sorts({TabName}.Order.createTime.desc)\n//				)\n//			);\n//        } catch (Exception e) {\n//            log.error(e.getMessage(), e);\n//            resultInfo.setCode(e);\n//        }\n//        return resultInfo;\n//    }\n}"
                    ,
                    "Controller.java"
            );
            DIR.TEMP.write(
                    "package com.business.{javaname}.service;\n\nimport lombok.NonNull;\nimport org.springframework.transaction.annotation.Transactional;\n/**\n * 服务接口实现类：\n *\n * @author Jason Xie on {date}.\n */\n@Service\n@Slf4j\npublic class {JavaName}Service implements IService<{TabName}, {ID}> {\n	@Autowired\n	private {JavaName}Repository repository;\n	@Transactional\n	@Override\n	public {TabName} save(final {TabName} obj, final String userId) throws Exception {\n		Asserts.notEmpty(obj, \"参数【obj】是必须的\");\n		Asserts.notEmpty(userId, \"参数【userId】是必须的\");\n		Params.notEmpty(obj.getName(), \"字段【name】不能为空\");\n		obj.setId(null);\n		obj.setCreateUserId(userId);\n		obj.setModifyUserId(userId);\n		return repository.save(obj);\n	}\n//	@Transactional\n//	@Override\n//	public List<{TabName}> save(final List<{TabName}> list, final String userId) throws Exception {\n//		Asserts.notEmpty(list, \"参数【list】不能为空\");\n//		Asserts.notEmpty(userId, \"参数【userId】是必须的\");\n//		return repository.saveAll(list);\n//	}\n//	@Transactional\n//	@Override\n//	public void update(final {ID} id, final String userId, final {TabName} obj) throws Exception {\n//		Asserts.notEmpty(id,\"参数【id】是必须的\");\n//		Asserts.notEmpty(userId,\"参数【userId】是必须的\");\n//		Asserts.notEmpty(obj, \"参数【obj】是必须的\");\n//		Params.notEmpty(obj.getName(), \"字段【name】不能为空\");\n//		UpdateRowsException.asserts(repository.update(id, userId, obj));\n//	}\n//	@Transactional\n//	@Override\n//	public {TabName} deleteById(final {ID} id, final String userId) throws Exception {\n//		Asserts.notEmpty(id, \"参数【id】是必须的\");\n//		Asserts.notEmpty(userId, \"参数【userId】是必须的\");\n//		return repository.deleteById(id);\n//	}\n//	@Transactional\n//	@Override\n//	public void markDeleteById(final {ID} id, final String userId) throws Exception {\n//		Params.notEmpty(id, \"字段【id】不能为空\");\n//		Asserts.notEmpty(userId, \"参数【userId】是必须的\");\n//		UpdateRowsException.asserts(repository.markDeleteById(id, userId));\n//	}\n//	@Transactional\n//	@Override\n//	public void markDeleteById(final List<{ID}> ids, final String userId) throws Exception {\n//		Params.notEmpty(ids, \"字段【ids】不能为空\");\n//		Asserts.notEmpty(userId, \"参数【userId】是必须的\");\n//		repository.markDeleteById(ids, userId);\n//	}\n//	@Override\n//	public Optional<{TabName}> getById(final {ID} id) throws Exception {\n//		Params.notEmpty(id, \"字段【id】不能为空\");\n//		return repository.findById(id);\n//	}\n//	@Override\n//	public List<{TabName}> findList(final {TabName} condition, Sorts... sorts) throws Exception {\n//		Asserts.notEmpty(condition, \"参数【condition】是必须的\");\n//        condition.setEmptyDeleted();\n//		return repository.findList(condition, sorts);\n//	}\n//	@Override\n//	public Page<{TabName}> findPage(final {TabName} condition, Pager pager) throws Exception {\n//		Asserts.notEmpty(condition, \"参数【condition】是必须的\");\n//        condition.setEmptyDeleted();\n//		return repository.findPage(condition, pager);\n//	}\n}"
                    ,
                    "Service.java"
            );
            DIR.TEMP.write(
                    "package com.business.{javaname}.dao.jpa;\n\nimport lombok.NonNull;\nimport org.springframework.data.jpa.repository.Query;\n\nimport javax.annotation.Nonnull;\n/**\n * 数据操作：\n *\n * @author Jason Xie on {date}\n */\npublic interface {JavaName}Repository extends\n		JpaRepository<{TabName}, {ID}>,\n		IRepository<{TabName}, {ID}>\n{\n	Q{TabName} q = Q{TabName}.{tabName};\n\n//	@Modifying\n//	@Query\n//	@Override\n//	default long update(@Nonnull final {ID} id, @Nonnull final String userId, @Nonnull final {TabName} obj) throws Exception {\n//		Asserts.notEmpty(userId, \"参数【userId】是必须的\");\n//		return jpaQueryFactory.<JPAQueryFactory>getBean()\n//			.update(q)\n//			.set(q.deleted, obj.getDeleted())\n//			.where(q.id.eq(id).and(q.createUserId.eq(userId)))\n//			.execute();\n//	}\n//	@Modifying\n//	@Query\n//	@Override\n//	default {TabName} deleteById(@NonNull final {ID} id, @NonNull final String userId) throws Exception {\n//		final Optional<{TabName}> optional = findById(id);\n//		if(optional.isPresent()) {\n//			final {TabName} obj=optional.get();\n//			Asserts.isTrue(obj.getCreateUserId().equals(userId), String.format(\"用户无权限操作该数据：【%s】\",id));\n//			delete(obj);\n//			return obj;\n//		} else {\n//			throw new NullPointerException(String.format(\"数据不存在：【%s】\",id));\n//		}\n//	}\n//	@Modifying\n//	@Query\n//	@Override\n//	default long markDeleteById(@Nonnull final {ID} id, @Nonnull final String userId) throws Exception {\n//		return jpaQueryFactory.<JPAQueryFactory>getBean()\n//			.update(q)\n//			.set(q.deleted, Switch.NO)\n//			.set(q.modifyUserId, userId)\n//			.where(q.id.eq(id).and(q.createUserId.eq(userId)))\n//			.execute();\n//	}\n//	@Modifying\n//	@Query\n//	@Override\n//	default long markDeleteById(@Nonnull final List<{ID}> ids, @Nonnull final String userId) throws Exception {\n//		return jpaQueryFactory.<JPAQueryFactory>getBean()\n//			.update(q)\n//			.set(q.deleted, Switch.NO)\n//			.set(q.modifyUserId, userId)\n//			.where(q.id.in(ids).and(q.createUserId.eq(userId)))\n//			.execute();\n//	}\n//	@Query\n//	@Override\n//	default List<{TabName}> findList(@NonNull final {TabName} condition, Sorts... sorts) throws Exception {\n//		return jpaQueryFactory.<JPAQueryFactory>getBean()\n//			.selectFrom(q)\n//			.where(condition.qdslWhere().toArray())\n//			.orderBy(Sorts.qdslOrder({TabName}.Order.createTime.desc, sorts))\n//			.fetch();\n//	}\n//	@Query\n//	@Override\n//	default Page<{TabName}> findPage(@NonNull final {TabName} condition, Pager pager) throws Exception {\n//		Pager.build(pager);\n//		final QueryResults<{TabName}> results = jpaQueryFactory.<JPAQueryFactory>getBean()\n//			.selectFrom(q)\n//			.where(condition.qdslWhere().toArray())\n//			.offset(pager.offset())\n//			.limit(pager.limit())\n//			.orderBy(Sorts.qdslOrder({TabName}.Order.createTime.desc, pager.getSorts()))\n//			.fetchResults();\n//		return new PageImpl<>(results.getResults(), pager.pageable(), results.getTotal());\n//	}\n\n}"
                    ,
                    "Repository.java"
            );
            DIR.TEMP.write(
                    "package com.business.{javaname}.entity;\n"+"\n"+"import com.alibaba.fastjson.annotation.JSONField;\n"+"import com.alibaba.fastjson.annotation.JSONType;\n"+"import com.support.entity.IUser;\n"+"import com.querydsl.core.annotations.QueryEntity;\n"+"import com.querydsl.core.types.OrderSpecifier;\n"+"import com.utils.common.entity.ITable;\n"+"import com.utils.common.entity.IWhere;\n"+"import com.utils.enums.Switch;\n"+"import lombok.AllArgsConstructor;\n"+"import lombok.Builder;\n"+"import lombok.Data;\n"+"import lombok.NoArgsConstructor;\n"+"import org.hibernate.annotations.DynamicInsert;\n"+"import org.hibernate.annotations.DynamicUpdate;\n"+"import org.hibernate.annotations.GenericGenerator;\n"+"\n"+"import javax.persistence.Column;\n"+"import javax.persistence.Entity;\n"+"import javax.persistence.GeneratedValue;\n"+"import javax.persistence.Table;\n"+"import javax.persistence.Id;\n"+"import java.sql.Timestamp;\n"+"\n"+"/**\n"+" * 实体：\n"+" *\n"+" * @author Jason Xie on {date}.\n"+" */\n"+"@Table(name = \"{tab_name}\")\n"+"@Entity\n"+"@DynamicInsert\n"+"@DynamicUpdate\n"+"@QueryEntity\n"+"@NoArgsConstructor\n"+"@AllArgsConstructor\n"+"@Builder\n"+"@Data\n"+"@JSONType(orders = {})\n"+"public class {TabName} implements ITable, {IUser} IWhere {\n"+"	/**\n"+"	 * 枚举：定义排序字段\n"+"	 */\n"+"	public enum Order {\n"+"//		createTime({tabName}.createTime.asc(), {tabName}.createTime.desc()),\n"+"//		modifyTime({tabName}.modifyTime.asc(), {tabName}.modifyTime.desc()),\n"+"		;\n"+"//		public final Sorts asc;\n"+"//		public final Sorts desc;\n"+"		Order(final OrderSpecifier qdslAsc, final OrderSpecifier qdsldesc) {\n"+"//			asc = Sorts.builder()\n"+"//					.qdsl(qdslAsc)\n"+"//					.jpa(Sort.by(Sort.Order.asc(this.name())))\n"+"//					.build();\n"+"//			desc = Sorts.builder()\n"+"//					.qdsl(qdsldesc)\n"+"//					.jpa(Sort.by(Sort.Order.desc(this.name())))\n"+"//					.build();\n"+"		}\n"+"	}\n"+"{props}\n"+"	@Override\n"+"	public String toString() {\n"+"		return json();\n"+"	}\n"+"\n"+"	@Override\n"+"	public QdslWheres qdslWhere() {\n"+"		Q{TabName} q = Q{TabName}.{tabName};\n"+"//		return IWhere.QdslWheres.build()\n"+"//				.and(createUserId, () -> q.createUserId.eq(createUserId))\n"+"//				.and(createTimeRange, () -> {\n"+"//					createTimeRange.rebuild();\n"+"//					return q.createTime.between(createTimeRange.getBegin(), createTimeRange.getEnd());\n"+"//				})\n"+"//				;\n"+"		return null;\n"+"	}\n"+"}"
                    ,
                    "Tab.java"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Config {
        private static void clear() {
            tab_name = null;
            TabName = null;
            tabName = null;
            java_name = null;
            JavaName = null;
            javaName = null;
            javaname = null;
        }
        private static void init(final String name) {
            tab_name = name;
            TabName = Stream.of(tab_name.split("_")).map(Util::firstUpper).collect(Collectors.joining());
            tabName = Util.firstLower(TabName);
            java_name = tab_name.replaceFirst("tab_", "").replace("_", "-");
            JavaName = Stream.of(tab_name.replaceFirst("tab_", "").split("_")).map(Util::firstUpper).collect(Collectors.joining());
            javaName = Util.firstLower(JavaName);
            javaname = JavaName.toLowerCase();
        }

        private static String format(final String content) {
            return content.replace("{date}", date)
                    .replace("{tab_name}", tab_name)
                    .replace("{TabName}", TabName)
                    .replace("{tabName}", tabName)
                    .replace("{javaName}", javaName)
                    .replace("{JavaName}", JavaName)
                    .replace("{java_name}", java_name)
                    .replace("{javaname}", javaname)
                    ;
        }

        private static String date = Dates.now().formatDate();
        private static String tab_name;
        private static String TabName;
        private static String tabName;
        private static String javaName;
        private static String JavaName;
        private static String java_name;
        private static String javaname;
    }
}
