//package web;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.anavss.medicine.config.WebMvcConfig;
//import com.anavss.medicine.config.enums.Keys;
//import com.anavss.medicine.support.util.JUnit;
//import com.utils.util.Maps;
//import com.utils.util.Util;
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//import model.ITest;
//import model.Tester;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//
///**
// * @author Jason Xie on 2017/10/13.
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {WebMvcConfig.class})
//@WebAppConfiguration
//@Slf4j
//public class ApiTestControllerTest implements ITest {
//    @Getter
//    private String urlPrefix = "/api/test";
//    @Getter
//    private MockMvc mockMvc;
//    @Autowired
//    private WebApplicationContext webApplicationContext;
//
//    @Before
//    public void init() {
//        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//        JUnit.set();
//    }
//
//    @Test
//    public void config() {
//        try {
//            Map<String, String> map = new LinkedHashMap<>();
//            for (Keys key : Keys.values()) {
//                map.put(key.name(), key.get());
//            }
//            writeFile(getMethodName(), JSON.toJSONString(map, SerializerFeature.PrettyFormat));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void codes() {
//        Tester.builder()
//                .clazz(this.getClass())
//                .methodName(getMethodName())
//                .mockMvc(mockMvc)
//                .url(urlPrefix)
//                .build()
//                .getById("codes")
//        ;
//    }
//
//    @Test
//    public void save() {
//        Tester.builder()
//                .clazz(this.getClass())
//                .methodName(getMethodName())
//                .mockMvc(mockMvc)
//                .url(urlPrefix)
//                .build()
//                .post(
//                        Maps.ofSO()
//                                .put("name", "Jason Xie")
//                                .put("phone", "18717942600")
//                                .build()
//                )
//        ;
//    }
//
//    @Test
//    public void update() {
//        Tester.builder()
//                .clazz(this.getClass())
//                .methodName(getMethodName())
//                .mockMvc(mockMvc)
//                .url(urlPrefix)
//                .build()
//                .updateById(
//                        Util.uuid(),
//                        Maps.ofSO()
//                                .put("name", "Jason Xie")
//                                .put("phone", "18717942600")
//                                .build()
//                )
//        ;
//    }
//
//    @Test
//    public void deleteById() {
//        Tester.builder()
//                .clazz(this.getClass())
//                .methodName(getMethodName())
//                .mockMvc(mockMvc)
//                .url(urlPrefix)
//                .build()
//                .deleteById(Util.uuid())
//        ;
//    }
//
//    @Test
//    public void markDeleteById() {
//        Tester.builder()
//                .clazz(this.getClass())
//                .methodName(getMethodName())
//                .mockMvc(mockMvc)
//                .url(urlPrefix)
//                .build()
//                .markDeleteById(Util.uuid())
//        ;
//    }
//
//    @Test
//    public void markDeleteByIds() {
//        Tester.builder()
//                .clazz(this.getClass())
//                .methodName(getMethodName())
//                .mockMvc(mockMvc)
//                .url(urlPrefix)
//                .build()
//                .markDeleteByIds(
//                        Util.uuid(),
//                        Util.uuid(),
//                        Util.uuid()
//                )
//        ;
//    }
//
//    @Test
//    public void getById() {
//        Tester.builder()
//                .clazz(this.getClass())
//                .methodName(getMethodName())
//                .mockMvc(mockMvc)
//                .url(urlPrefix)
//                .build()
//                .getById(Util.uuid())
//        ;
//    }
//
//    @Test
//    public void search() {
//        Tester.builder()
//                .clazz(this.getClass())
//                .methodName(getMethodName())
//                .mockMvc(mockMvc)
//                .url(urlPrefix)
//                .build()
//                .get(
//                        Maps.ofSO()
//                                .put("name", "Jason Xie")
//                                .put("phone", "18717942600")
//                                .buildJSONObject()
//                )
//        ;
//    }
//
//    @Test
//    public void searchPage() {
//        Tester.builder()
//                .clazz(this.getClass())
//                .methodName(getMethodName())
//                .mockMvc(mockMvc)
//                .url(urlPrefix)
//                .build()
//                .search(1, 100,
//                        Maps.ofSO()
//                                .put("name", "Jason Xie")
//                                .put("phone", "18717942600")
//                                .buildJSONObject()
//                )
//        ;
//    }
//}
