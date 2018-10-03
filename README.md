# common-utils

## 安装步骤
下载源码
```
git clone https://gitee.com/xcc/common-utils.git
```
编译安装 jar 包到本地仓库
```
mvn install
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

### 注解说明
```
>> 类头部注解
@Table(name = "table_name")：注解数据库表名
@Entity：注解spring data jpa扫描的实体
@Document：注解spring data mongo扫描的实体
@QueryEntity：注解生成 QueryDSL 实体，Q{ClassName}.java
@DynamicInsert：注解声明，编译生成 insert 语句时，当字段为 null ，则被忽略 
@DynamicUpdate：注解声明，编译生成 update 语句时，当字段为 null ，则被忽略
@NoArgsConstructor：注解生成无参构造函数
@AllArgsConstructor：注解生成全参构造函数
@RequiredArgsConstructor(staticName = "of")：注解生成 final 修饰字段或者是以 @NonNull 声明字段 的静态构造函数，函数名为 of ；前提是类不能有以下注解 @NoArgsConstructor，@AllArgsConstructor
@Builder：注解生成类构造器，支持链式调用
@Data：注解生成 get & set 方法
@Accessors(chain = {true|false})：注解生成 set 方法时返回当前对象，便于链式调用
@Accessors(fluent = {true|false})：注解生成 get & set 方法时不要 get & set 前缀
@JSONType(orders = {"id","name"})：注解声明实体类字段排序，响应时会按照此处声明的顺序排序；警告：必须声明所有返回字段的顺序，否则此声明不起作用
@Slf4j：注解生成 log 属性，可在类中通过 log.{debug|info} 输出日志
 
>> 方法注解
@Synchronized：注解给方法加上同步锁
@SneakyThrows：注解声明自动抛异常，不需要 在方法上加 throw {Exception|NullPointException|IOException}
 
>> 属性注解
@Setter：注解生成当前属性的 get 方法
@Getter：注解生成当前属性的 set 方法
@Transient > @org.springframework.data.annotation.Transient：spirng-data mongodb 注解声明忽略字段，不执行 insert 和 update
@Transient > @javax.persistence.Transient：spirng-data jpa hibernate 注解声明忽略字段，不执行 insert 和 update
@Indexed > @org.springframework.data.mongodb.core.index.Indexed：注解声明 mongodb 数据库生成索引
 
>> 属性或方法都适用的注解
@JSONField(serialize = {true|false})：注解声明此字段在 JSON.toJSONString() 时是否被忽略，默认 true，为 false 则被忽略
@JSONField(deserialize = {true|false})：注解声明此字段在 JSON.parseObject() 时是否被忽略，默认 true，为 false 则被忽略
@Column(insertable = {true|false})：注解声明此字段在 执行 数据库 insert 时是否强制忽略（不论是否有值），默认 true，为 false 则强制忽略
@Column(updatable = {true|false})：注解声明此字段在 执行 数据库 update 时是否强制忽略（不论是否有值），默认 true，为 false 则强制忽略
@Id > @org.springframework.data.annotation.Id：注解声明 mongodb 实体ID
@Id > @javax.persistence.Id：注解声明 jpa hibernate 实体ID
@GeneratedValue(generator = "uuid2")：注解声明 jpa hibernate 实体ID生成器名称，与 @GenericGenerator 组合使用
@GenericGenerator(name = "uuid2", strategy = "uuid2")：注解声明 jpa hibernate 实体ID生成器实现及名称
@QueryTransient：注解声明生成 Q{ClassName}.java 时忽略字段，Q{ClassName}.java 将不包含此字段
```
