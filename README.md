# common-utils

JAVA 开发常用的工具类封装，并不是每一处都合理，欢迎大家来共同优化  
项目依赖 lombok，若编译不通过，请检查是否已安装 lombok 插件  
底部会对一封装的类进行索引说明；
请尊重开源，转载请注明来源：
https://gitee.com/xcc/common-utils  

## 安装步骤
下载源码
```
git clone https://gitee.com/xcc/common-utils.git
```
编译安装 jar 包到本地仓库
```
mvn install
# mvn install 会默认会打包源码并跳过测试
# mvn source:jar install -Dmaven.test.skip=true
```
maven 依赖
```
<dependency>
    <groupId>com.utils</groupId>
    <artifactId>common-utils</artifactId>
    <version>1.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```


### 常用注解说明
```
>> 类头部注解
@Table(name = "table_name")：注解映射数据库表名
@Entity：声明数据库映射实体类，将会被Spring JPA扫描
@Document：声明 MongoDB 映射实体类，将会被Spring Mongo扫描
@QueryEntity：声明 QueryDSL 实体类，将会被 QueryDSL 通用查询框架扫描，生成Q{ClassName}.java
@DynamicInsert：声明，编译生成 insert 语句时，当字段为 null ，则被忽略 
@DynamicUpdate：声明，编译生成 update 语句时，当字段为 null ，则被忽略
// @DynamicUpdate
// 这里有坑，当数据库字段有值，希望把数据库字段设置为 null，这种情况需要使用其他解决方案；
// 方案1：数据库设置时，数字默认 0，字符串默认 '' ；需要置空时，实体类设置属性为默认的 0 和 ''；
//   优点：代码量少逻辑简单；
//   缺点：JPA 只支持 ID 字段作为更新条件
// 方案2【推荐】：代码构建需要更新的字段，因为数据库有些字段可能不适合设置默认值
//   优点：更灵活，场景可适配；大部分场景下，只有ID作为更新匹配条件无法满足需求
//   缺点：代码量增加

@NoArgsConstructor：生成无参构造函数
@AllArgsConstructor：生成全参构造函数
@RequiredArgsConstructor(staticName = "of")：生成 final 修饰字段或者是以 @Nonnull 声明字段的静态构造函数，函数名为 of ；前提是类不能有以下注解 @NoArgsConstructor，@AllArgsConstructor
@Builder：生成链式类构造器
@Data：生成 get & set & toString & hashCode & equals 方法
@Accessors(chain = {true|false})：声明 @Data & @Setter 注解生成 set 方法时返回 this，便于链式调用；
// @Accessors(chain = true)
// 这里有坑，使用 QueryDSL 框架的 Projections.bean 时，因为 set 方法都带返回值，所以出现检测不到 set 方法
// 所以不建议在数据库映射的实体类中使用该注解；下面是采坑后的测试代码
// Stream.of(Introspector.getBeanInfo(Item.class).getPropertyDescriptors()).forEach(prop -> System.out.println(String.format("%s :----> %s", prop.getWriteMethod(), prop.getReadMethod())));
 
@Accessors(fluent = {true|false})：声明 @Data & @Setter & @Getter 注解生成 get & set 方法时不要 get & set 前缀
@JSONType(orders = {"id","name"})：声明实体类属性在 JSON 序列化时的排序；警告：必须声明所有返回字段的顺序，否则此声明不起作用；版本升级后再次测试发现该 bug 有修正，会对 orders 的字段优先，orders 不存在的字段才会乱序
@Slf4j：注解生成 log 属性，可在类中通过 log.{debug|info} 输出日志
 
>> 方法注解
@Synchronized：给方法加上同步锁
@SneakyThrows：声明自动抛异常，不需要 在方法上加 throw {Exception|NullPointException|IOException}
 
>> 属性注解
@Setter：生成当前属性的 get 方法
@Getter：生成当前属性的 set 方法
@Transient > @org.springframework.data.annotation.Transient：spirng-data mongodb 声明 JPA + Mongo 不与数据库建立映射，且 insert 和 update 忽略该属性
@Transient > @javax.persistence.Transient：spirng-data jpa hibernate 声明 JPA + Hibernate 不与数据库建立映射，且 insert 和 update 忽略该属性
@Indexed > @org.springframework.data.mongodb.core.index.Indexed：声明 mongodb 数据库生成索引
 
>> 属性或 get 方法都适用的注解
@JSONField(serialize = {true|false})：声明该属性或 get 方法在 JSON.toJSONString() 时是否被忽略，默认 true；为 false 表示忽略
@JSONField(deserialize = {true|false})：声明该属性或 set 方法在 JSON.parseObject() 时是否被忽略，默认 true；为 false 表示忽略
@JSONField(fromat = "yyyy-MM-dd HH:mm:ss")：声明 JSON.toJSONString() | JSON.parseObject() 时的日期格式
@Column(insertable = {true|false})：声明当 JPA 执行数据库 insert 时是否强制忽略（不论是否有值），默认 true，为 false 则强制忽略
@Column(updatable = {true|false})：声明当 JPA 执行数据库 update 时是否强制忽略（不论是否有值），默认 true，为 false 则强制忽略
@Id > @org.springframework.data.annotation.Id：注解声明 mongodb 实体ID
@Id > @javax.persistence.Id：注解声明 jpa hibernate 实体ID
@QueryTransient：声明生成 Q{ClassName}.java 时忽略该属性
```

### 常用类说明，一般类底部都会有 main 方法测试
com.utils.IJson:接口:实现该接口附加默认的json格式化操作，有默认实现；依赖：https://github.com/alibaba/fastjson  
com.utils.enums.Charsets:枚举:定义编码类型  
com.utils.enums.Colors:枚举:定义颜色  
com.utils.enums.ContentType:枚举:定义http请求和响应类型  
com.utils.enums.HttpState:枚举:定义http响应状态码  
com.utils.enums.Image:枚举:定义图片类型  
com.utils.excel.enums.Column:类:定义Excel操作列  
com.utils.excel.enums.Formula:类:Excel公式封装  
com.utils.excel.CellStyles:类:Excel写入单元格样式  
com.utils.excel.CloneStyles:类:指定Excel写入样式来源文件，写入时从该来源克隆样式  
com.utils.excel.ExcelReader:类:封装Excel读操作，支持xls和xlsx，xls格式不支持依赖其他文件的样式；依赖：https://github.com/apache/poi  
com.utils.excel.ExcelRewriter:类:封装Excel边读边写操作，支持xls和xlsx，xls格式不支持依赖其他文件的样式；依赖：https://github.com/apache/poi  
com.utils.excel.Rownum:类:定义行号行索引封装类，因为普通读写使用索引，公式使用的是行号  
com.utils.excel.SSheetWriter:类:带缓冲区限制的Excel表写入，当需要全表刷新公式的表格不能使用该方式写入，否则会抛出异常；最大缓存行依赖于实例化 new SXSSFWorkbook(max) 指定的值；max 默认值为100  
com.utils.excel.XSheetWriter:类:不带缓冲区限制的Excel表写入，数据量太大时容易OOM，支持全表刷新公式（带公式的单元格写入时不执行刷新操作则不会计算值）  
com.utils.util.Base64:类:Base64编码解码  
com.utils.util.CodeImage:类:验证码生成  
com.utils.util.Dates:类:日期操作  
com.utils.util.Dates#Range:类:日期区间操作  
com.utils.util.FCopy:类:文件复制  
com.utils.util.FPath:类:文件路径构造及基本操作  
com.utils.util.FWrite:类:文件写操作  
com.utils.util.FZip:类:文件及文件夹压缩操作  
com.utils.util.HtmlPdfWriter:类:将html页面写入pdf；依赖：https://github.com/itext/itextpdf  
com.utils.util.JSEngine:类:挂起JS引擎，执行JS代码    
com.utils.util.Maps:类:链式构建Map  
com.utils.util.Num:类:数字操作
com.utils.util.Num#Range:类:数字区间操作  
com.utils.util.Num#RangeInt:类:int数字区间操作  
com.utils.util.Num#RangeLong:类:long数字区间操作  
com.utils.util.QRCode:类:二维码生成；依赖：https://github.com/zxing/zxing    
com.utils.util.Util:类:常用基础方法封装  