package com.utils.enums;

import com.utils.ICode;

/**
 * 全局响应代码，异常响应代码
 * 定义 com.support.mvc.entity.base.Result#setCode(Code) 返回编码
 *
 * @author 谢长春 2019-1-9
 */
public enum Code implements ICode {
    A00000("成功"),
    A00001("失败"),
    A00002("自定义异常，将会以 exception 内容替换 message 内容，一般用于抛出带动态参数消息，直接在前端弹窗"),
    A00003("会话超时"),
    A00004("参数校验失败"),
    A00005("接口版本号不匹配"),
    A00006("请求地址不存在"),
    A00007("请求缺少必要的参数"),
    A00008("请求参数类型不匹配或 JSON 格式不符合规范"),
    A00009("请求方式不支持"),
    A00010("请求不存在"),
    A00011("无操作权限"),
    A00012("排序字段不在可选范围"),
    A00013("分页查询，每页显示数据量超过最大值"),
    A00014("上传文件列表为空"),
    ;
    /**
     * 枚举属性说明
     */
    public final String comment;

    Code(String comment) {
        this.comment = comment;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    //    /**
    //     * 通过 Code 构建 Result 对象；注：只能构建 Result<Object>，若要指定泛型，请使用 new Result<?> 指定泛型
    //     *
    //     * @return Result<Object>
    //     */
    //    public <E> Result<E> toResult() {
    //        return new Result<>(this);
    //    }
    //
    //    /**
    //     * 通过 Code 构建 Result 对象；注：只能构建 Result<Object>，若要指定泛型，请使用 new Result<?> 指定泛型
    //     *
    //     * @param exception String 异常消息，可选参数，
    //     * @return Result<Object>
    //     */
    //    public <E> Result<E> toResult(final String exception) {
    //        return new Result<E>(this).setException(exception);
    //    }
    //
    //    /**
    //     * 构造并返回 CodeException
    //     *
    //     * @param exception String 异常消息内容
    //     * @return CodeException
    //     */
    //    public CodeException exception(final String exception) {
    //        return new CodeException(this, exception);
    //    }
    //
    //    /**
    //     * 构造并返回 CodeException
    //     *
    //     * @param exception String 异常消息内容
    //     * @param throwable Throwable 异常堆栈
    //     * @return CodeException
    //     */
    //    public CodeException exception(final String exception, final Throwable throwable) {
    //        return new CodeException(this, exception, throwable);
    //    }
    //
    //    public static void main(String[] args) {
    //        { // 构建 js 枚举文件
    //            val name = "枚举：响应状态码";
    //            StringBuilder sb = new StringBuilder();
    //            sb.append("/**\n")
    //                    .append(" * ").append(name).append("\n")
    //                    .append(String.format(" * Created by 谢长春 on %s.%n", Dates.now().formatDate()))
    //                    .append(" */\n");
    //            sb.append("// 枚举值定义").append("\n");
    //            sb.append("const status = Object.freeze({").append("\n");
    //            Stream.of(Code.values()).forEach(item -> sb.append(
    //                    "\t{name}: {value: '{name}', comment: '{comment}'},"
    //                            .replace("{name}", item.name())
    //                            .replace("{comment}", item.comment)
    //                    ).append("\n")
    //            );
    //            sb.append("\tgetComment:function(key){return (this[key]||{}).comment}\n});").append("\n");
    //            sb.append("// 枚举值转换为选项集合").append("\n");
    //            sb.append("const options = [").append("\n");
    //            Stream.of(Code.values()).forEach(item -> sb.append(
    //                    "\t{value: status.{name}.value, label: status.{name}.comment},"
    //                            .replace("{name}", item.name())
    //                    ).append("\n")
    //            );
    //            sb.append("];").append("\n");
    //            sb.append("export default status;");
    //            System.out.println("JS文件输出路径：\n" +
    //                    FWrite.of("logs", Code.class.getSimpleName().concat(".js"))
    //                            .write(sb.toString())
    //                            .getAbsolute().orElse(null)
    //            );
    //        }
    //
    //        System.out.println(
    //                Stream.of(Code.values())
    //                        .map(item -> String.format("%s【%s】", item.name(), item.comment))
    //                        .collect(Collectors.joining("|"))
    //        );
    //    }
}
