package com.example.demo;

import com.example.demo.dto.AuthRequestDto;
import com.example.demo.dto.RegisterRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.*;
import com.example.demo.servlet.SimpleStatusServlet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
// import lombok.var;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@Listeners({TestResultListener.class})
public class MultiLocationInventoryBalancerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryLevelRepository inventoryLevelRepository;
    @Autowired
    private DemandForecastRepository demandForecastRepository;
    @Autowired
    private TransferSuggestionRepository transferSuggestionRepository;

    @Autowired
    private AuthService authService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryLevelService inventoryLevelService;
    @Autowired
    private DemandForecastService demandForecastService;
    @Autowired
    private InventoryBalancerService inventoryBalancerService;

    private String adminToken;

    // ---------------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------------

    private String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private String bearer() {
        return "Bearer " + adminToken;
    }

    private String uniqueStoreName(String base) {
        return base + "-" + UUID.randomUUID();
    }

    private String uniqueSku(String base) {
        return base + "-" + UUID.randomUUID();
    }

    private String uniqueProductName(String base) {
        return base + "-" + UUID.randomUUID();
    }

    // ---------------------------------------------------------------------
    // Setup admin user and token
    // ---------------------------------------------------------------------

    @BeforeClass(alwaysRun = true)
    public void initAdminUserAndToken() throws Exception {
        // Register admin once (ignore duplicate)
        try {
            RegisterRequestDto reg = new RegisterRequestDto();
            reg.setEmail("admin@invbalancer.com");
            reg.setFullName("Admin User");
            reg.setPassword("adminpass123");
            reg.setRole("ROLE_ADMIN");
            authService.register(reg);
        } catch (BadRequestException ignored) {
        }

        // Login via HTTP to get real JWT
        AuthRequestDto login = new AuthRequestDto();
        login.setEmail("admin@invbalancer.com");
        login.setPassword("adminpass123");

        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJson(login)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode node = objectMapper.readTree(body);
        adminToken = node.get("token").asText();

        Assert.assertNotNull(adminToken, "Admin JWT must not be null");
    }

    // =====================================================================
    // 1. Servlet tests – SimpleStatusServlet using doGet()
    // =====================================================================

    @Test(priority = 1, groups = "servlet")
    public void t01_servlet_doGet_returnsExpectedMessage() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        SimpleStatusServlet servlet = new SimpleStatusServlet();
        servlet.doGet(req, resp);

        String content = resp.getContentAsString();
        Assert.assertEquals(
                content,
                "Multi-Location Inventory Balancer is running"
        );
    }

    @Test(priority = 2, groups = "servlet")
    public void t02_servlet_doGet_setsPlainTextContentType() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        SimpleStatusServlet servlet = new SimpleStatusServlet();
        servlet.doGet(req, resp);

        Assert.assertEquals(resp.getContentType(), "text/plain");
    }

    @Test(priority = 3, groups = "servlet")
    public void t03_servlet_doGet_statusIsOk() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        SimpleStatusServlet servlet = new SimpleStatusServlet();
        servlet.doGet(req, resp);

        Assert.assertEquals(resp.getStatus(), HttpServletResponse.SC_OK);
    }

    @Test(priority = 4, groups = "servlet")
    public void t04_servlet_multipleCalls_sameOutput() throws Exception {
        SimpleStatusServlet servlet = new SimpleStatusServlet();

        MockHttpServletRequest req1 = new MockHttpServletRequest();
        MockHttpServletResponse resp1 = new MockHttpServletResponse();
        servlet.doGet(req1, resp1);

        MockHttpServletRequest req2 = new MockHttpServletRequest();
        MockHttpServletResponse resp2 = new MockHttpServletResponse();
        servlet.doGet(req2, resp2);

        Assert.assertEquals(resp1.getContentAsString(), resp2.getContentAsString());
    }

    @Test(priority = 5, groups = "servlet")
    public void t05_servlet_responseNotEmpty() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        SimpleStatusServlet servlet = new SimpleStatusServlet();
        servlet.doGet(req, resp);

        String content = resp.getContentAsString();
        Assert.assertFalse(content.isEmpty());
    }

    @Test(priority = 6, groups = "servlet")
    public void t06_servlet_responseContainsNoHtml() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        SimpleStatusServlet servlet = new SimpleStatusServlet();
        servlet.doGet(req, resp);

        String content = resp.getContentAsString();
        Assert.assertFalse(content.contains("<html>"));
    }

    // =====================================================================
    // 2. CRUD operations using Spring Boot + REST APIs
    // =====================================================================

    @Test(priority = 10, groups = "crud")
    public void t07_auth_login_validCredentials_returnsToken() throws Exception {
        AuthRequestDto login = new AuthRequestDto();
        login.setEmail("admin@invbalancer.com");
        login.setPassword("adminpass123");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJson(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test(priority = 11, groups = "crud")
    public void t08_auth_login_invalidPassword_fails() throws Exception {
        AuthRequestDto login = new AuthRequestDto();
        login.setEmail("admin@invbalancer.com");
        login.setPassword("wrong-password");

        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJson(login)))
                .andReturn();

        int status = result.getResponse().getStatus();
        Assert.assertTrue(status >= 400);
    }

    @Test(priority = 12, groups = "crud")
    public void t09_createStore_viaController_success() throws Exception {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Main Store"));
        store.setAddress("Central Road");
        store.setRegion("North");

        mockMvc.perform(
                        post("/api/stores")
                                .header("Authorization", bearer())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJson(store)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").exists())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test(priority = 13, groups = "crud")
    public void t10_createProduct_viaController_success() throws Exception {
        Product product = new Product();
        product.setSku(uniqueSku("SKU-MLIB-001"));
        product.setName(uniqueProductName("Balanced Item"));
        product.setCategory("Category-A");

        mockMvc.perform(
                        post("/api/products")
                                .header("Authorization", bearer())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJson(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").exists());
    }

    @Test(priority = 14, groups = "crud")
    public void t11_listStores_returnsArray() throws Exception {
        mockMvc.perform(
                        get("/api/stores")
                                .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test(priority = 15, groups = "crud")
    public void t12_listProducts_returnsArray() throws Exception {
        mockMvc.perform(
                        get("/api/products")
                                .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test(priority = 16, groups = "crud")
public void t13_updateStore_changesRegion() {
    // Create an initial store
    Store store = new Store();
    store.setStoreName(uniqueStoreName("Region Store-MLIB"));
    store.setAddress("R Street");
    store.setRegion("OldR");
    store = storeService.createStore(store);

    // Prepare update (we allow storeName change – as per your choice B)
    Store update = new Store();
    update.setStoreName(uniqueStoreName("Region Store-Updated"));
    update.setAddress("New R Street");
    update.setRegion("NewR");
    update.setActive(true); // keep active so no validation fails

    // Call service-level update
    Store updated = storeService.updateStore(store.getId(), update);

    // Assertions
    Assert.assertNotNull(updated.getId());
    Assert.assertEquals(updated.getRegion(), "NewR");
    Assert.assertEquals(updated.getStoreName(), update.getStoreName());
}


    @Test(priority = 17, groups = "crud")
public void t14_deactivateStore_setsActiveFalse() {
    // Create a store
    Store store = new Store();
    store.setStoreName(uniqueStoreName("Temp Store-MLIB"));
    store.setAddress("Temp Address");
    store.setRegion("TR");
    store = storeService.createStore(store);

    // Deactivate via service
    storeService.deactivateStore(store.getId());

    // Reload and assert
    Store reloaded = storeService.getStoreById(store.getId());
    Assert.assertFalse(reloaded.isActive(), "Store should be inactive after deactivation");
}



   @Test(priority = 18, groups = "crud")
public void t15_deactivateProduct_setsActiveFalse() {
    // Create a product
    Product product = new Product();
    product.setSku(uniqueSku("SKU-MLIB-002"));
    product.setName(uniqueProductName("Temp Product-MLIB"));
    product.setCategory("TempCat");
    product = productService.createProduct(product);

    // Deactivate via service
    productService.deactivateProduct(product.getId());

    // Reload and assert
    Product reloaded = productService.getProductById(product.getId());
    Assert.assertFalse(reloaded.isActive(), "Product should be inactive after deactivation");
}



    @Test(priority = 19, groups = "crud")
    public void t16_createInventory_forStoreAndProduct_success() throws Exception {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Inv-Store-A"));
        store.setAddress("A Street");
        store.setRegion("R1");
        store = storeService.createStore(store);

        Product product = new Product();
        product.setSku(uniqueSku("SKU-INV-A"));
        product.setName(uniqueProductName("Inventory-A"));
        product.setCategory("C1");
        product = productService.createProduct(product);

        InventoryLevel inv = new InventoryLevel();
        inv.setStore(store);
        inv.setProduct(product);
        inv.setQuantity(40);

        mockMvc.perform(
                        post("/api/inventory")
                                .header("Authorization", bearer())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJson(inv)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(40));
    }

    @Test(priority = 20, groups = "crud")
    public void t17_updateInventory_changesQuantity() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Inv-Store-B"));
        store.setAddress("B Street");
        store.setRegion("R2");
        store = storeService.createStore(store);

        Product product = new Product();
        product.setSku(uniqueSku("SKU-INV-B"));
        product.setName(uniqueProductName("Inventory-B"));
        product.setCategory("C2");
        product = productService.createProduct(product);

        InventoryLevel inv = new InventoryLevel();
        inv.setStore(store);
        inv.setProduct(product);
        inv.setQuantity(10);
        inventoryLevelService.createOrUpdateInventory(inv);

        InventoryLevel update = new InventoryLevel();
        update.setStore(store);
        update.setProduct(product);
        update.setQuantity(25);

        InventoryLevel result = inventoryLevelService.createOrUpdateInventory(update);
        Assert.assertEquals(result.getQuantity().intValue(), 25);
    }

    @Test(priority = 21, groups = "crud")
    public void t18_createForecast_futureDate_success() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Forecast-Store-A"));
        store.setAddress("FS Street");
        store.setRegion("R3");
        store = storeService.createStore(store);

        Product product = new Product();
        product.setSku(uniqueSku("SKU-FCAST-001"));
        product.setName(uniqueProductName("ForecastItem-1"));
        product.setCategory("C3");
        product = productService.createProduct(product);

        DemandForecast forecast = new DemandForecast();
        forecast.setStore(store);
        forecast.setProduct(product);
        forecast.setForecastedDemand(120);
        forecast.setForecastDate(LocalDate.now().plusDays(2));

        DemandForecast saved = demandForecastService.createForecast(forecast);
        Assert.assertNotNull(saved.getId());
    }

    @Test(priority = 22, groups = "crud")
    public void t19_createForecast_pastDate_throwsBadRequest() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Forecast-Store-B"));
        store.setAddress("F2 Street");
        store.setRegion("R4");
        store = storeService.createStore(store);

        Product product = new Product();
        product.setSku(uniqueSku("SKU-FCAST-002"));
        product.setName(uniqueProductName("ForecastItem-2"));
        product.setCategory("C4");
        product = productService.createProduct(product);

        DemandForecast forecast = new DemandForecast();
        forecast.setStore(store);
        forecast.setProduct(product);
        forecast.setForecastedDemand(50);
        forecast.setForecastDate(LocalDate.now().minusDays(1));

        try {
            demandForecastService.createForecast(forecast);
            Assert.fail("Expected BadRequestException not thrown");
        } catch (BadRequestException ex) {
            Assert.assertTrue(true);
        }
    }

    @Test(priority = 23, groups = "crud")
    public void t20_getInventoryForStore_returnsList() {
        List<Store> stores = storeRepository.findAll();
        if (stores.isEmpty()) {
            Store s = new Store();
            s.setStoreName(uniqueStoreName("Inv-Store-C"));
            s.setRegion("R5");
            s.setAddress("C Street");
            storeService.createStore(s);
        }
        Store any = storeRepository.findAll().get(0);
        List<InventoryLevel> list = inventoryLevelService.getInventoryForStore(any.getId());
        Assert.assertNotNull(list);
    }

    // =====================================================================
    // 3. Dependency Injection & IoC tests
    // =====================================================================

    @Test(priority = 30, groups = "di")
    public void t21_di_mockMvcInjected() {
        Assert.assertNotNull(mockMvc);
    }

    @Test(priority = 31, groups = "di")
    public void t22_di_servicesInjected() {
        Assert.assertNotNull(storeService);
        Assert.assertNotNull(productService);
        Assert.assertNotNull(inventoryLevelService);
        Assert.assertNotNull(demandForecastService);
        Assert.assertNotNull(inventoryBalancerService);
    }

    @Test(priority = 32, groups = "di")
    public void t23_di_repositoriesInjected() {
        Assert.assertNotNull(storeRepository);
        Assert.assertNotNull(productRepository);
        Assert.assertNotNull(inventoryLevelRepository);
        Assert.assertNotNull(demandForecastRepository);
        Assert.assertNotNull(transferSuggestionRepository);
    }

    @Test(priority = 33, groups = "di")
    public void t24_di_authAndJwtInjected() {
        Assert.assertNotNull(authService);
        Assert.assertNotNull(jwtUtil);
    }

    @Test(priority = 34, groups = "di")
    public void t25_di_userAccountRepositoryInjected() {
        Assert.assertNotNull(userAccountRepository);
    }

    // =====================================================================
    // 4. Hibernate configurations, lifecycle & CRUD behavior
    // =====================================================================

    @Test(priority = 40, groups = "hibernate")
    public void t26_userAccount_prePersist_setsTimestamps() {
        UserAccount user = new UserAccount();
        user.setEmail(uniqueProductName("lifecycle@test.com"));
        user.setPassword("pass");
        user.setRole("ROLE_USER");
        user.prePersist();

        Assert.assertNotNull(user.getCreatedAt());
        Assert.assertNotNull(user.getUpdatedAt());
    }

    @Test(priority = 41, groups = "hibernate")
    public void t27_userAccount_preUpdate_updatesTimestamp() {
        UserAccount user = new UserAccount();
        user.setEmail(uniqueProductName("update@test.com"));
        user.setPassword("pass");
        user.setRole("ROLE_USER");
        user.prePersist();
        var before = user.getUpdatedAt();

        user.preUpdate();
        var after = user.getUpdatedAt();

        Assert.assertTrue(after.equals(before) || after.isAfter(before));
    }

    @Test(priority = 42, groups = "hibernate")
    public void t28_inventoryLevel_prePersist_setsLastUpdated() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Lifecycle-Store-1"));
        store.setAddress("L Street");
        store.setRegion("LR1");
        store = storeService.createStore(store);

        Product product = new Product();
        product.setSku(uniqueSku("SKU-LIFE-001"));
        product.setName(uniqueProductName("LifeProduct-1"));
        product.setCategory("LC");
        product = productService.createProduct(product);

        InventoryLevel inv = new InventoryLevel();
        inv.setStore(store);
        inv.setProduct(product);
        inv.setQuantity(15);
        InventoryLevel saved = inventoryLevelRepository.save(inv);

        Assert.assertNotNull(saved.getLastUpdated());
    }

    @Test(priority = 43, groups = "hibernate")
    public void t29_inventoryLevel_update_changesLastUpdatedOrSame() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Lifecycle-Store-2"));
        store.setAddress("L2 Street");
        store.setRegion("LR2");
        store = storeService.createStore(store);

        Product product = new Product();
        product.setSku(uniqueSku("SKU-LIFE-002"));
        product.setName(uniqueProductName("LifeProduct-2"));
        product.setCategory("LC2");
        product = productService.createProduct(product);

        InventoryLevel inv = new InventoryLevel();
        inv.setStore(store);
        inv.setProduct(product);
        inv.setQuantity(5);
        InventoryLevel saved = inventoryLevelRepository.save(inv);

        var before = saved.getLastUpdated();
        saved.setQuantity(25);
        InventoryLevel updated = inventoryLevelRepository.save(saved);
        var after = updated.getLastUpdated();

        Assert.assertTrue(after.equals(before) || after.isAfter(before));
    }

    @Test(priority = 44, groups = "hibernate")
    public void t30_transferSuggestion_prePersist_setsGeneratedAt() {
        Product product = new Product();
        product.setSku(uniqueSku("SKU-TS-001"));
        product.setName(uniqueProductName("TS Item"));
        product.setCategory("TSC");
        product = productService.createProduct(product);

        Store src = new Store();
        src.setStoreName(uniqueStoreName("TS-Store-Src"));
        src.setRegion("TSR1");
        src.setAddress("TS A");
        src = storeService.createStore(src);

        Store tgt = new Store();
        tgt.setStoreName(uniqueStoreName("TS-Store-Tgt"));
        tgt.setRegion("TSR2");
        tgt.setAddress("TS B");
        tgt = storeService.createStore(tgt);

        TransferSuggestion ts = new TransferSuggestion();
        ts.setProduct(product);
        ts.setSourceStore(src);
        ts.setTargetStore(tgt);
        ts.setSuggestedQuantity(10);
        ts.setReason("Lifecycle test");

        TransferSuggestion saved = transferSuggestionRepository.save(ts);
        Assert.assertNotNull(saved.getGeneratedAt());
    }

    @Test(priority = 45, groups = "hibernate")
    public void t31_store_defaultActiveTrue() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("ActiveCheckStore"));
        Assert.assertTrue(store.isActive());
    }

    @Test(priority = 46, groups = "hibernate")
    public void t32_product_defaultActiveTrue() {
        Product product = new Product();
        product.setSku(uniqueSku("SKU-ACT-001"));
        product.setName(uniqueProductName("ActiveProduct"));
        Assert.assertTrue(product.isActive());
    }

    @Test(priority = 47, groups = "hibernate")
    public void t33_inventory_negativeQuantity_throwsBadRequest() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Neg-Store"));
        store.setRegion("NEG");
        store.setAddress("Neg A");
        store = storeService.createStore(store);

        Product product = new Product();
        product.setSku(uniqueSku("SKU-NEG-001"));
        product.setName(uniqueProductName("NegItem"));
        product.setCategory("NEG");
        product = productService.createProduct(product);

        InventoryLevel inv = new InventoryLevel();
        inv.setStore(store);
        inv.setProduct(product);
        inv.setQuantity(-5);

        try {
            inventoryLevelService.createOrUpdateInventory(inv);
            Assert.fail("Expected BadRequestException not thrown");
        } catch (BadRequestException ex) {
            Assert.assertTrue(true);
        }
    }

    // =====================================================================
    // 5. JPA mapping & normalization
    // =====================================================================

    @Test(priority = 50, groups = "mapping")
    public void t34_inventoryLevel_storeProductMappingCorrect() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Map-Store-1"));
        store.setRegion("MR1");
        store.setAddress("M1");
        store = storeService.createStore(store);

        Product product = new Product();
        product.setSku(uniqueSku("SKU-MAP-001"));
        product.setName(uniqueProductName("MappedItem-1"));
        product.setCategory("MC1");
        product = productService.createProduct(product);

        InventoryLevel inv = new InventoryLevel();
        inv.setStore(store);
        inv.setProduct(product);
        inv.setQuantity(30);
        InventoryLevel saved = inventoryLevelService.createOrUpdateInventory(inv);

        Assert.assertEquals(saved.getStore().getId(), store.getId());
        Assert.assertEquals(saved.getProduct().getId(), product.getId());
    }

    @Test(priority = 51, groups = "mapping")
    public void t35_demandForecast_storeProductMappingCorrect() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Map-Store-2"));
        store.setRegion("MR2");
        store.setAddress("M2");
        store = storeService.createStore(store);

        Product product = new Product();
        product.setSku(uniqueSku("SKU-MAP-002"));
        product.setName(uniqueProductName("MappedItem-2"));
        product.setCategory("MC2");
        product = productService.createProduct(product);

        DemandForecast f = new DemandForecast();
        f.setStore(store);
        f.setProduct(product);
        f.setForecastedDemand(80);
        f.setForecastDate(LocalDate.now().plusDays(4));
        DemandForecast saved = demandForecastService.createForecast(f);

        Assert.assertEquals(saved.getStore().getId(), store.getId());
        Assert.assertEquals(saved.getProduct().getId(), product.getId());
    }

    @Test(priority = 52, groups = "mapping")
    public void t36_inventoryLevel_uniqueStoreProduct_updatesExisting() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Map-Store-3"));
        store.setRegion("MR3");
        store.setAddress("M3");
        store = storeService.createStore(store);

        Product product = new Product();
        product.setSku(uniqueSku("SKU-MAP-003"));
        product.setName(uniqueProductName("MappedItem-3"));
        product.setCategory("MC3");
        product = productService.createProduct(product);

        InventoryLevel inv1 = new InventoryLevel();
        inv1.setStore(store);
        inv1.setProduct(product);
        inv1.setQuantity(10);
        inventoryLevelService.createOrUpdateInventory(inv1);

        InventoryLevel inv2 = new InventoryLevel();
        inv2.setStore(store);
        inv2.setProduct(product);
        inv2.setQuantity(50);
        InventoryLevel updated = inventoryLevelService.createOrUpdateInventory(inv2);

        Assert.assertEquals(updated.getQuantity().intValue(), 50);
    }

    @Test(priority = 53, groups = "mapping")
    public void t37_normalization_storeNamesDistinct() {
        Store s1 = new Store();
        s1.setStoreName(uniqueStoreName("Norm-Store-1"));
        s1.setAddress("NS1");
        s1.setRegion("NR");
        storeService.createStore(s1);

        Store s2 = new Store();
        s2.setStoreName(uniqueStoreName("Norm-Store-2"));
        s2.setAddress("NS2");
        s2.setRegion("NR");
        storeService.createStore(s2);

        Assert.assertNotEquals(s1.getStoreName(), s2.getStoreName());
    }

    @Test(priority = 54, groups = "mapping")
    public void t38_normalization_productSkuAndNameDistinct() {
        Product p1 = new Product();
        p1.setSku(uniqueSku("SKU-NORM-001"));
        p1.setName(uniqueProductName("NormItem-1"));
        p1.setCategory("N1");
        productService.createProduct(p1);

        Product p2 = new Product();
        p2.setSku(uniqueSku("SKU-NORM-002"));
        p2.setName(uniqueProductName("NormItem-2"));
        p2.setCategory("N2");
        productService.createProduct(p2);

        Assert.assertNotEquals(p1.getSku(), p2.getSku());
        Assert.assertNotEquals(p1.getName(), p2.getName());
    }

    @Test(priority = 55, groups = "mapping")
    public void t39_getInventoryForProduct_returnsList() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            Product p = new Product();
            p.setSku(uniqueSku("SKU-MAP-004"));
            p.setName(uniqueProductName("MappedItem-4"));
            p.setCategory("MC4");
            productService.createProduct(p);
        }
        Product any = productRepository.findAll().get(0);
        List<InventoryLevel> list = inventoryLevelService.getInventoryForProduct(any.getId());
        Assert.assertNotNull(list);
    }

    @Test(priority = 56, groups = "mapping")
    public void t40_getForecastsForStore_returnsList() {
        List<Store> stores = storeRepository.findAll();
        if (stores.isEmpty()) {
            Store s = new Store();
            s.setStoreName(uniqueStoreName("Map-Store-4"));
            s.setRegion("MR4");
            s.setAddress("M4");
            storeService.createStore(s);
        }
        Store any = storeRepository.findAll().get(0);
        List<DemandForecast> list = demandForecastService.getForecastsForStore(any.getId());
        Assert.assertNotNull(list);
    }

    // =====================================================================
    // 6. Many-to-Many style relations via mapping entities
    // =====================================================================

    @Test(priority = 60, groups = "manyToMany")
    public void t41_singleProduct_multipleStores_viaInventory() {
        Product product = new Product();
        product.setSku(uniqueSku("SKU-MTM-001"));
        product.setName(uniqueProductName("MTMProduct-1"));
        product.setCategory("MTM");
        product = productService.createProduct(product);

        Store s1 = new Store();
        s1.setStoreName(uniqueStoreName("MTM-Store-1"));
        s1.setRegion("MMR1");
        s1.setAddress("MM1");
        s1 = storeService.createStore(s1);

        Store s2 = new Store();
        s2.setStoreName(uniqueStoreName("MTM-Store-2"));
        s2.setRegion("MMR2");
        s2.setAddress("MM2");
        s2 = storeService.createStore(s2);

        InventoryLevel inv1 = new InventoryLevel();
        inv1.setStore(s1);
        inv1.setProduct(product);
        inv1.setQuantity(20);
        inventoryLevelService.createOrUpdateInventory(inv1);

        InventoryLevel inv2 = new InventoryLevel();
        inv2.setStore(s2);
        inv2.setProduct(product);
        inv2.setQuantity(40);
        inventoryLevelService.createOrUpdateInventory(inv2);

        List<InventoryLevel> all = inventoryLevelRepository.findByProduct_Id(product.getId());
        Assert.assertTrue(all.size() >= 2);
    }

    @Test(priority = 61, groups = "manyToMany")
    public void t42_singleStore_multipleProducts_viaInventory() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("MTM-Store-3"));
        store.setRegion("MMR3");
        store.setAddress("MM3");
        store = storeService.createStore(store);

        Product p1 = new Product();
        p1.setSku(uniqueSku("SKU-MTM-002"));
        p1.setName(uniqueProductName("MTM-P1"));
        p1.setCategory("MTM");
        p1 = productService.createProduct(p1);

        Product p2 = new Product();
        p2.setSku(uniqueSku("SKU-MTM-003"));
        p2.setName(uniqueProductName("MTM-P2"));
        p2.setCategory("MTM");
        p2 = productService.createProduct(p2);

        InventoryLevel inv1 = new InventoryLevel();
        inv1.setStore(store);
        inv1.setProduct(p1);
        inv1.setQuantity(5);
        inventoryLevelService.createOrUpdateInventory(inv1);

        InventoryLevel inv2 = new InventoryLevel();
        inv2.setStore(store);
        inv2.setProduct(p2);
        inv2.setQuantity(9);
        inventoryLevelService.createOrUpdateInventory(inv2);

        List<InventoryLevel> list = inventoryLevelRepository.findByStore_Id(store.getId());
        Assert.assertTrue(list.size() >= 2);
    }

    @Test(priority = 62, groups = "manyToMany")
    public void t43_multipleStores_forecastsForSameProduct() {
        Product product = new Product();
        product.setSku(uniqueSku("SKU-MTM-004"));
        product.setName(uniqueProductName("MTM-Forecast-Item"));
        product.setCategory("MTM");
        product = productService.createProduct(product);

        Store s1 = new Store();
        s1.setStoreName(uniqueStoreName("MTM-F-Store-1"));
        s1.setRegion("MFR1");
        s1.setAddress("MF1");
        s1 = storeService.createStore(s1);

        Store s2 = new Store();
        s2.setStoreName(uniqueStoreName("MTM-F-Store-2"));
        s2.setRegion("MFR2");
        s2.setAddress("MF2");
        s2 = storeService.createStore(s2);

        DemandForecast f1 = new DemandForecast();
        f1.setStore(s1);
        f1.setProduct(product);
        f1.setForecastedDemand(30);
        f1.setForecastDate(LocalDate.now().plusDays(2));
        demandForecastService.createForecast(f1);

        DemandForecast f2 = new DemandForecast();
        f2.setStore(s2);
        f2.setProduct(product);
        f2.setForecastedDemand(60);
        f2.setForecastDate(LocalDate.now().plusDays(2));
        demandForecastService.createForecast(f2);

        List<DemandForecast> list = demandForecastRepository.findByProduct_Id(product.getId());
        Assert.assertTrue(list.size() >= 2);
    }

    @Test(priority = 63, groups = "manyToMany")
    public void t44_store_participates_in_inventory_and_forecast() {
        Store store = new Store();
        store.setStoreName(uniqueStoreName("Rel-Store-1"));
        store.setRegion("RS1");
        store.setAddress("RS");
        store = storeService.createStore(store);

        Product p1 = new Product();
        p1.setSku(uniqueSku("SKU-REL-001"));
        p1.setName(uniqueProductName("Rel-Item-1"));
        p1.setCategory("REL");
        p1 = productService.createProduct(p1);

        Product p2 = new Product();
        p2.setSku(uniqueSku("SKU-REL-002"));
        p2.setName(uniqueProductName("Rel-Item-2"));
        p2.setCategory("REL");
        p2 = productService.createProduct(p2);

        InventoryLevel inv = new InventoryLevel();
        inv.setStore(store);
        inv.setProduct(p1);
        inv.setQuantity(7);
        inventoryLevelService.createOrUpdateInventory(inv);

        DemandForecast f = new DemandForecast();
        f.setStore(store);
        f.setProduct(p2);
        f.setForecastedDemand(15);
        f.setForecastDate(LocalDate.now().plusDays(5));
        demandForecastService.createForecast(f);

        List<InventoryLevel> invList = inventoryLevelRepository.findByStore_Id(store.getId());
        List<DemandForecast> fcList = demandForecastRepository.findByStore_Id(store.getId());

        Assert.assertFalse(invList.isEmpty());
        Assert.assertFalse(fcList.isEmpty());
    }

    @Test(priority = 64, groups = "manyToMany")
    public void t45_inventory_and_forecast_enableManyToManyConceptually() {
        List<InventoryLevel> invList = inventoryLevelRepository.findAll();
        List<DemandForecast> fcList = demandForecastRepository.findAll();

        Assert.assertNotNull(invList);
        Assert.assertNotNull(fcList);
    }

    // =====================================================================
    // 7. Security & JWT token-based authentication
    // =====================================================================

    @Test(priority = 70, groups = "security")
    public void t46_jwt_generateToken_and_extractUsername() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 999L);
        claims.put("role", "ROLE_TEST");

        String token = jwtUtil.generateToken(claims, "jwtuser@test.com");
        Assert.assertNotNull(token);

        String username = jwtUtil.getUsername(token);
        Assert.assertEquals(username, "jwtuser@test.com");
    }

    @Test(priority = 71, groups = "security")
    public void t47_jwt_isTokenValid_forCorrectUser() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1000L);

        String token = jwtUtil.generateToken(claims, "valid@test.com");
        boolean valid = jwtUtil.isTokenValid(token, "valid@test.com");
        Assert.assertTrue(valid);
    }

    @Test(priority = 72, groups = "security")
    public void t48_jwt_isTokenInvalid_forWrongUser() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1001L);

        String token = jwtUtil.generateToken(claims, "one@test.com");
        boolean valid = jwtUtil.isTokenValid(token, "two@test.com");
        Assert.assertFalse(valid);
    }

    @Test(priority = 73, groups = "security")
    public void t49_jwt_expirationMillis_positive() {
        long exp = jwtUtil.getExpirationMillis();
        Assert.assertTrue(exp > 0);
    }

    @Test(priority = 74, groups = "security")
    public void t50_accessApiWithoutToken_isRejected() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/stores"))
                .andReturn();
        int status = result.getResponse().getStatus();
        Assert.assertTrue(status >= 400);
    }

    @Test(priority = 75, groups = "security")
    public void t51_accessApiWithToken_isAllowed() throws Exception {
        mockMvc.perform(
                        get("/api/stores")
                                .header("Authorization", bearer()))
                .andExpect(status().isOk());
    }

    @Test(priority = 76, groups = "security")
    public void t52_registerDuplicateEmail_throwsBadRequest() {
        RegisterRequestDto reg = new RegisterRequestDto();
        reg.setEmail("admin@invbalancer.com");
        reg.setFullName("Another");
        reg.setPassword("somePassword1");
        reg.setRole("ROLE_ADMIN");

        try {
            authService.register(reg);
            Assert.fail("Expected BadRequestException not thrown");
        } catch (BadRequestException ex) {
            Assert.assertTrue(true);
        }
    }

    @Test(priority = 77, groups = "security")
    public void t53_userAccount_findByEmail_returnsAdmin() {
        UserAccount user = userAccountRepository
                .findByEmail("admin@invbalancer.com")
                .orElse(null);

        Assert.assertNotNull(user);
        Assert.assertEquals(user.getEmail(), "admin@invbalancer.com");
    }

    @Test(priority = 78, groups = "security")
    public void t54_loginWithWrongPassword_throwsException() {
        AuthRequestDto dto = new AuthRequestDto();
        dto.setEmail("admin@invbalancer.com");
        dto.setPassword("invalid-pass");

        try {
            authService.login(dto);
            Assert.fail("Expected exception not thrown");
        } catch (Exception ex) {
            Assert.assertTrue(true);
        }
    }

    @Test(priority = 79, groups = "security")
    public void t55_publicEndpoints_authAndSwaggerAccessibleWithoutToken() throws Exception {
        MvcResult swaggerResult = mockMvc.perform(get("/swagger-ui/index.html"))
                .andReturn();
        Assert.assertTrue(swaggerResult.getResponse().getStatus() < 500);

        MvcResult loginResult = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"admin@invbalancer.com\",\"password\":\"adminpass123\"}"))
                .andReturn();
        Assert.assertEquals(loginResult.getResponse().getStatus(), 200);
    }

    // =====================================================================
    // 8. HQL-style queries & advanced data querying
    // =====================================================================

    @Test(priority = 80, groups = "hql")
    public void t56_repo_findInventoryByStoreId() {
        List<Store> stores = storeRepository.findAll();
        if (stores.isEmpty()) {
            Store s = new Store();
            s.setStoreName(uniqueStoreName("HQL-Store-1"));
            s.setRegion("HQLR1");
            s.setAddress("H1");
            storeService.createStore(s);
        }
        Store any = storeRepository.findAll().get(0);
        List<InventoryLevel> list = inventoryLevelRepository.findByStore_Id(any.getId());
        Assert.assertNotNull(list);
    }

    @Test(priority = 81, groups = "hql")
    public void t57_repo_findForecastsByStoreId() {
        List<Store> stores = storeRepository.findAll();
        if (stores.isEmpty()) {
            Store s = new Store();
            s.setStoreName(uniqueStoreName("HQL-Store-2"));
            s.setRegion("HQLR2");
            s.setAddress("H2");
            storeService.createStore(s);
        }
        Store any = storeRepository.findAll().get(0);
        List<DemandForecast> list = demandForecastRepository.findByStore_Id(any.getId());
        Assert.assertNotNull(list);
    }

    @Test(priority = 82, groups = "hql")
    public void t58_repo_findInventoryByProductId() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            Product p = new Product();
            p.setSku(uniqueSku("SKU-HQL-001"));
            p.setName(uniqueProductName("HQL-Item-1"));
            p.setCategory("HQLC");
            productService.createProduct(p);
        }
        Product any = productRepository.findAll().get(0);
        List<InventoryLevel> list = inventoryLevelRepository.findByProduct_Id(any.getId());
        Assert.assertNotNull(list);
    }

    @Test(priority = 83, groups = "hql")
    public void t59_repo_findSuggestionsByProductId() {
        Product product = new Product();
        product.setSku(uniqueSku("SKU-HQL-002"));
        product.setName(uniqueProductName("HQL-Item-2"));
        product.setCategory("HQLC2");
        product = productService.createProduct(product);

        Store src = new Store();
        src.setStoreName(uniqueStoreName("HQL-Src-Store"));
        src.setRegion("HQL-SR");
        src.setAddress("HS");
        src = storeService.createStore(src);

        Store tgt = new Store();
        tgt.setStoreName(uniqueStoreName("HQL-Tgt-Store"));
        tgt.setRegion("HQL-TR");
        tgt.setAddress("HT");
        tgt = storeService.createStore(tgt);

        TransferSuggestion ts = new TransferSuggestion();
        ts.setProduct(product);
        ts.setSourceStore(src);
        ts.setTargetStore(tgt);
        ts.setSuggestedQuantity(3);
        ts.setReason("HQL test");
        transferSuggestionRepository.save(ts);

        List<TransferSuggestion> list = transferSuggestionRepository.findByProduct_Id(product.getId());
        Assert.assertFalse(list.isEmpty());
    }

    @Test(priority = 84, groups = "hql")
    public void t60_balancer_generateSuggestions_basicFlow() {
        Product product = new Product();
        product.setSku(uniqueSku("SKU-BAL-001"));
        product.setName(uniqueProductName("Bal-Item-1"));
        product.setCategory("BAL");
        product = productService.createProduct(product);

        Store overStore = new Store();
        overStore.setStoreName(uniqueStoreName("Bal-Over-Store"));
        overStore.setRegion("BR1");
        overStore.setAddress("BO");
        overStore = storeService.createStore(overStore);

        Store underStore = new Store();
        underStore.setStoreName(uniqueStoreName("Bal-Under-Store"));
        underStore.setRegion("BR2");
        underStore.setAddress("BU");
        underStore = storeService.createStore(underStore);

        InventoryLevel overInv = new InventoryLevel();
        overInv.setStore(overStore);
        overInv.setProduct(product);
        overInv.setQuantity(120);
        inventoryLevelService.createOrUpdateInventory(overInv);

        InventoryLevel underInv = new InventoryLevel();
        underInv.setStore(underStore);
        underInv.setProduct(product);
        underInv.setQuantity(10);
        inventoryLevelService.createOrUpdateInventory(underInv);

        DemandForecast overForecast = new DemandForecast();
        overForecast.setStore(overStore);
        overForecast.setProduct(product);
        overForecast.setForecastedDemand(30);
        overForecast.setForecastDate(LocalDate.now().plusDays(1));
        demandForecastService.createForecast(overForecast);

        DemandForecast underForecast = new DemandForecast();
        underForecast.setStore(underStore);
        underForecast.setProduct(product);
        underForecast.setForecastedDemand(90);
        underForecast.setForecastDate(LocalDate.now().plusDays(1));
        demandForecastService.createForecast(underForecast);

        List<TransferSuggestion> suggestions =
                inventoryBalancerService.generateSuggestions(product.getId());
        Assert.assertFalse(suggestions.isEmpty());
    }

    @Test(priority = 85, groups = "hql")
    public void t61_balancer_generateSuggestions_inactiveProduct_throwsBadRequest() {
        Product product = new Product();
        product.setSku(uniqueSku("SKU-BAL-002"));
        product.setName(uniqueProductName("Bal-Item-2"));
        product.setCategory("BAL2");
        product = productService.createProduct(product);

        product.setActive(false);
        productRepository.save(product);

        try {
            inventoryBalancerService.generateSuggestions(product.getId());
            Assert.fail("Expected BadRequestException not thrown");
        } catch (BadRequestException ex) {
            Assert.assertTrue(true);
        }
    }

    @Test(priority = 86, groups = "hql")
    public void t62_balancer_getSuggestionById_notFoundThrows() {
        try {
            inventoryBalancerService.getSuggestionById(999999L);
            Assert.fail("Expected ResourceNotFoundException not thrown");
        } catch (ResourceNotFoundException ex) {
            Assert.assertTrue(true);
        }
    }

    @Test(priority = 87, groups = "hql")
    public void t63_repositories_store_findAll_notNull() {
        List<Store> list = storeRepository.findAll();
        Assert.assertNotNull(list);
    }

    @Test(priority = 88, groups = "hql")
    public void t64_repositories_product_findAll_notNull() {
        List<Product> list = productRepository.findAll();
        Assert.assertNotNull(list);
    }

    @Test(priority = 89, groups = "hql")
    public void t65_repositories_forecast_inventory_suggestions_notNull() {
        List<DemandForecast> fList = demandForecastRepository.findAll();
        List<InventoryLevel> iList = inventoryLevelRepository.findAll();
        List<TransferSuggestion> tList = transferSuggestionRepository.findAll();

        Assert.assertNotNull(fList);
        Assert.assertNotNull(iList);
        Assert.assertNotNull(tList);
    }
}
