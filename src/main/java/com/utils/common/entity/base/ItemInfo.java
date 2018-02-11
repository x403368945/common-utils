package com.utils.common.entity.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import com.utils.util.Num;
import com.utils.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 通用简单对象
 * 
 * @author Jason Xie 2016-11-23
 * 
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true)
@JSONType(orders = { "label", "value", "childs"})
public class ItemInfo{
	private String label;
	private Object value;
	private List<ItemInfo> childs;

	public int intValue() {return Num.of(value).intValue();}
	public int intValue(Number defaultValue) {return Num.of(Util.tostring(value), defaultValue).intValue();}
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
