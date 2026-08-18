package kg.dev.pixel_commerce_ai.telegram;

import kg.dev.pixel_commerce_ai.dto.AiPurchaseAdviceRequest;
import kg.dev.pixel_commerce_ai.dto.AiPurchaseAdviceResponse;
import kg.dev.pixel_commerce_ai.dto.DashboardResponse;
import kg.dev.pixel_commerce_ai.dto.ProductAnalyticsResponse;
import kg.dev.pixel_commerce_ai.dto.SaleItemRequest;
import kg.dev.pixel_commerce_ai.dto.SaleRequest;
import kg.dev.pixel_commerce_ai.entity.Product;
import kg.dev.pixel_commerce_ai.entity.ProductCondition;
import kg.dev.pixel_commerce_ai.entity.ProductStatus;
import kg.dev.pixel_commerce_ai.entity.Stock;
import kg.dev.pixel_commerce_ai.entity.StockBatch;
import kg.dev.pixel_commerce_ai.repository.ProductRepository;
import kg.dev.pixel_commerce_ai.repository.StockBatchRepository;
import kg.dev.pixel_commerce_ai.repository.StockRepository;
import kg.dev.pixel_commerce_ai.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PixelCommerceTelegramBot
        implements SpringLongPollingBot {

    private final TelegramClient telegramClient;

    private final AnalyticsService analyticsService;

    private final AiPurchaseAdvisorService aiPurchaseAdvisorService;

    private final SaleService saleService;

    private final ProductRepository productRepository;

    private final StockRepository stockRepository;

    private final StockBatchRepository stockBatchRepository;

    private final StockService stockService;

    private final AnalyticsResetService analyticsResetService;

    @Value("${telegram.bot.token}")
    private String botToken;

    /*
     * =========================================================
     * STATES
     * =========================================================
     */

    private final Map<Long, SaleState> saleStates =
            new ConcurrentHashMap<>();

    private final Map<Long, DeleteState> deleteStates =
            new ConcurrentHashMap<>();

    private final Map<Long, AddProductState> addProductStates =
            new ConcurrentHashMap<>();


    /*
     * =========================================================
     * BOT TOKEN
     * =========================================================
     */

    @Override
    public String getBotToken() {
        return botToken;
    }


    /*
     * =========================================================
     * UPDATE CONSUMER
     * =========================================================
     */

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {

        return updates -> {

            for (Update update : updates) {

                /*
                 * =================================================
                 * CALLBACK QUERY
                 * =================================================
                 */

                if (update.hasCallbackQuery()) {

                    handleCallback(update);

                    continue;
                }


                /*
                 * =================================================
                 * MESSAGE
                 * =================================================
                 */

                if (!update.hasMessage()) {
                    continue;
                }

                if (!update.getMessage().hasText()) {
                    continue;
                }

                String text =
                        update.getMessage()
                                .getText()
                                .trim();

                Long chatId =
                        update.getMessage()
                                .getChatId();

                System.out.println(
                        "TELEGRAM MESSAGE: [" + text + "]"
                );


                /*
                 * =================================================
                 * START
                 * =================================================
                 */

                if (text.startsWith("/start")) {

                    clearStates(chatId);

                    sendMainMenu(chatId);

                    continue;
                }


                /*
                 * =================================================
                 * CANCEL
                 * =================================================
                 */

                if (isCommand(
                        text,
                        "отмена",
                        "/отмена",
                        "❌ Отмена"
                )) {

                    clearStates(chatId);

                    sendMessage(
                            chatId,
                            "❌ Операция отменена."
                    );

                    continue;
                }


                /*
                 * =================================================
                 * ANALYTICS
                 * =================================================
                 */

                if (isCommand(
                        text,
                        "аналитика",
                        "/аналитика",
                        "/analytics",
                        "📊 Аналитика"
                )) {

                    clearStates(chatId);

                    sendAnalytics(chatId);

                    continue;
                }


                /*
                 * =================================================
                 * STOCK
                 * =================================================
                 */

                if (isCommand(
                        text,
                        "остатки",
                        "/остатки",
                        "/stock",
                        "📦 Остатки"
                )) {

                    clearStates(chatId);

                    sendStock(chatId);

                    continue;
                }


                /*
                 * =================================================
                 * PRODUCTS
                 * =================================================
                 */

                if (isCommand(
                        text,
                        "товары",
                        "/товары",
                        "/products",
                        "📱 Товары"
                )) {

                    clearStates(chatId);

                    sendProducts(chatId);

                    continue;
                }


                /*
                 * =================================================
                 * PROFIT
                 * =================================================
                 */

                if (isCommand(
                        text,
                        "прибыль",
                        "/прибыль",
                        "/profit",
                        "📈 Прибыль"
                )) {

                    clearStates(chatId);

                    sendProfit(chatId);

                    continue;
                }


                /*
                 * =================================================
                 * ADD PHONE
                 * =================================================
                 */

                if (isCommand(
                        text,
                        "добавить телефон",
                        "добавить товар",
                        "/добавить",
                        "/add",
                        "📱 Добавить телефон",
                        "📱 Добавить товар"
                )) {

                    startAddProduct(chatId);

                    continue;
                }


                /*
                 * =================================================
                 * SALE
                 * =================================================
                 */

                if (isCommand(
                        text,
                        "продажа",
                        "/продажа",
                        "/sale",
                        "💰 Продажа"
                )) {

                    startSale(chatId);

                    continue;
                }


                /*
                 * =================================================
                 * DELETE FROM STOCK
                 * =================================================
                 */

                if (isCommand(
                        text,
                        "удалить со склада",
                        "/удалить",
                        "/delete",
                        "🗑 Удалить со склада"
                )) {

                    startDeleteFromStock(chatId);

                    continue;
                }


                /*
                 * =================================================
                 * RESET ANALYTICS
                 * =================================================
                 */

                if (isCommand(
                        text,
                        "сбросить аналитику",
                        "/reset_analytics",
                        "🧹 Сбросить аналитику"
                )) {

                    clearStates(chatId);

                    sendAnalyticsResetConfirmation(chatId);

                    continue;
                }


                /*
                 * =================================================
                 * AI
                 * =================================================
                 */

                if (isCommand(
                        text,
                        "ai",
                        "/ai",
                        "🤖 AI"
                )) {

                    sendMessage(
                            chatId,
                            """
                            🤖 AI-СОВЕТНИК
                            
                            Введи курс CNY и бюджет.
                            
                            Например:
                            /ai 12.5 100000
                            """
                    );

                    continue;
                }


                /*
                 * =================================================
                 * AI COMMAND
                 * =================================================
                 */

                if (text.startsWith("/ai ")) {

                    handleAi(
                            chatId,
                            text
                    );

                    continue;
                }


                /*
                 * =================================================
                 * ADD PRODUCT STATE
                 * =================================================
                 *
                 * До этого места доходят только обычные сообщения,
                 * которые не являются кнопками меню.
                 */

                if (addProductStates.containsKey(chatId)) {

                    handleAddProductStep(
                            chatId,
                            text
                    );

                    continue;
                }


                /*
                 * =================================================
                 * DELETE STATE
                 * =================================================
                 */

                if (deleteStates.containsKey(chatId)) {

                    handleDeleteStep(
                            chatId,
                            text
                    );

                    continue;
                }


                /*
                 * =================================================
                 * SALE STATE
                 * =================================================
                 */

                if (saleStates.containsKey(chatId)) {

                    handleSaleStep(
                            chatId,
                            text
                    );

                    continue;
                }


                /*
                 * =================================================
                 * UNKNOWN COMMAND
                 * =================================================
                 */

                sendMessage(
                        chatId,
                        """
                        🤖 Не понял команду.
                        
                        Используй кнопки меню 👇
                        """
                );
            }
        };
    }

    private void sendAnalyticsResetConfirmation(
            Long chatId
    ) {

        InlineKeyboardButton confirm =
                InlineKeyboardButton
                        .builder()
                        .text("✅ Да, сбросить")
                        .callbackData(
                                "analytics_reset_confirm"
                        )
                        .build();

        InlineKeyboardButton cancel =
                InlineKeyboardButton
                        .builder()
                        .text("❌ Отмена")
                        .callbackData("cancel")
                        .build();

        InlineKeyboardMarkup keyboard =
                InlineKeyboardMarkup
                        .builder()
                        .keyboard(
                                List.of(
                                        new InlineKeyboardRow(
                                                confirm,
                                                cancel
                                        )
                                )
                        )
                        .build();

        SendMessage message =
                SendMessage.builder()
                        .chatId(chatId)
                        .text(
                                """
                                ⚠️ СБРОС АНАЛИТИКИ
                                
                                Будет удалена история продаж.
                                
                                ❗ Выручка → 0
                                ❗ Прибыль → 0
                                ❗ Продажи → 0
                                
                                📦 Товары на складе:
                                НЕ будут удалены.
                                
                                📦 Stock:
                                НЕ будет изменён.
                                
                                📦 StockBatch:
                                НЕ будет изменён.
                                
                                Продолжить?
                                """
                        )
                        .replyMarkup(keyboard)
                        .build();

        execute(message);
    }


    /*
     * =========================================================
     * MAIN MENU
     * =========================================================
     */

    private void sendMainMenu(Long chatId) {

        SendMessage message =
                SendMessage.builder()
                        .chatId(chatId)
                        .text(
                                """
                                🤖 Pixel Commerce AI
                                
                                Привет! 👋
                                
                                Управляй магазином
                                прямо через Telegram.
                                
                                Выбери нужный раздел 👇
                                """
                        )
                        .replyMarkup(
                                createMainKeyboard()
                        )
                        .build();

        execute(message);
    }


    private ReplyKeyboardMarkup createMainKeyboard() {

        KeyboardRow row1 =
                new KeyboardRow();

        row1.add("📊 Аналитика");
        row1.add("📦 Остатки");


        KeyboardRow row2 =
                new KeyboardRow();

        row2.add("📈 Прибыль");
        row2.add("📱 Товары");


        KeyboardRow row3 =
                new KeyboardRow();

        row3.add("📱 Добавить телефон");
        row3.add("💰 Продажа");


        KeyboardRow row4 =
                new KeyboardRow();

        row4.add("🗑 Удалить со склада");


        KeyboardRow row5 =
                new KeyboardRow();

        row5.add("🧹 Сбросить аналитику");

        KeyboardRow row6 =
                new KeyboardRow();

        row6.add("🤖 AI");


        return ReplyKeyboardMarkup
                .builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .keyboardRow(row3)
                .keyboardRow(row4)
                .keyboardRow(row5)
                .resizeKeyboard(true)
                .build();
    }


    /*
     * =========================================================
     * ADD PRODUCT
     * =========================================================
     */

    private void startAddProduct(Long chatId) {

        clearStates(chatId);

        AddProductState state =
                new AddProductState();

        state.step =
                AddProductStep.BRAND;

        addProductStates.put(
                chatId,
                state
        );

        sendMessage(
                chatId,
                """
                📱 ДОБАВЛЕНИЕ ТЕЛЕФОНА
                
                Введи бренд:
                
                Например:
                Google
                """
        );
    }


    private void handleAddProductStep(
            Long chatId,
            String text
    ) {

        AddProductState state =
                addProductStates.get(chatId);

        if (state == null) {
            return;
        }

        try {

            switch (state.step) {

                /*
                 * BRAND
                 */

                case BRAND -> {

                    if (text.isBlank()) {

                        sendMessage(
                                chatId,
                                "❌ Бренд не может быть пустым."
                        );

                        return;
                    }

                    state.brand =
                            text.trim();

                    state.step =
                            AddProductStep.MODEL;

                    sendMessage(
                            chatId,
                            """
                            📱 Введи модель:
                            
                            Например:
                            Pixel 9 Pro
                            """
                    );
                }


                /*
                 * MODEL
                 */

                case MODEL -> {

                    if (text.isBlank()) {

                        sendMessage(
                                chatId,
                                "❌ Модель не может быть пустой."
                        );

                        return;
                    }

                    state.model =
                            text.trim();

                    state.step =
                            AddProductStep.STORAGE;

                    sendMessage(
                            chatId,
                            """
                            💾 Введи объём памяти в GB:
                            
                            Например:
                            128
                            """
                    );
                }


                /*
                 * STORAGE
                 */

                case STORAGE -> {

                    int storage =
                            Integer.parseInt(
                                    text.trim()
                            );

                    if (storage <= 0) {

                        sendMessage(
                                chatId,
                                "❌ Память должна быть больше 0."
                        );

                        return;
                    }

                    state.storage =
                            storage;

                    state.step =
                            AddProductStep.CONDITION;

                    sendConditionKeyboard(chatId);
                }


                /*
                 * PURCHASE PRICE
                 */

                case PURCHASE_PRICE -> {

                    BigDecimal price =
                            new BigDecimal(
                                    text.trim()
                            );

                    if (price.compareTo(
                            BigDecimal.ZERO
                    ) <= 0) {

                        sendMessage(
                                chatId,
                                "❌ Цена закупки должна быть больше 0."
                        );

                        return;
                    }

                    state.purchasePrice =
                            price;

                    state.step =
                            AddProductStep.SALE_PRICE;

                    sendMessage(
                            chatId,
                            """
                            💰 Введи цену продажи
                            за одну штуку в KGS:
                            
                            Например:
                            68000
                            """
                    );
                }


                /*
                 * SALE PRICE
                 */

                case SALE_PRICE -> {

                    BigDecimal price =
                            new BigDecimal(
                                    text.trim()
                            );

                    if (price.compareTo(
                            BigDecimal.ZERO
                    ) <= 0) {

                        sendMessage(
                                chatId,
                                "❌ Цена продажи должна быть больше 0."
                        );

                        return;
                    }

                    state.salePrice =
                            price;

                    state.step =
                            AddProductStep.QUANTITY;

                    sendMessage(
                            chatId,
                            """
                            📦 Введи количество:
                            
                            Например:
                            1
                            """
                    );
                }


                /*
                 * QUANTITY
                 */

                case QUANTITY -> {

                    int quantity =
                            Integer.parseInt(
                                    text.trim()
                            );

                    if (quantity <= 0) {

                        sendMessage(
                                chatId,
                                "❌ Количество должно быть больше 0."
                        );

                        return;
                    }

                    state.quantity =
                            quantity;

                    state.step =
                            AddProductStep.SHIPPING_COST;

                    sendMessage(
                            chatId,
                            """
                            🚚 Введи общую стоимость доставки
                            за всю партию.
                            
                            Если доставки нет:
                            0
                            """
                    );
                }


                /*
                 * SHIPPING
                 */

                case SHIPPING_COST -> {

                    BigDecimal shipping =
                            new BigDecimal(
                                    text.trim()
                            );

                    if (shipping.compareTo(
                            BigDecimal.ZERO
                    ) < 0) {

                        sendMessage(
                                chatId,
                                "❌ Доставка не может быть отрицательной."
                        );

                        return;
                    }

                    state.shippingCost =
                            shipping;

                    state.step =
                            AddProductStep.OTHER_EXPENSES;

                    sendMessage(
                            chatId,
                            """
                            📦 Введи прочие расходы
                            за всю партию.
                            
                            Если расходов нет:
                            0
                            """
                    );
                }


                /*
                 * OTHER EXPENSES
                 */

                case OTHER_EXPENSES -> {

                    BigDecimal expenses =
                            new BigDecimal(
                                    text.trim()
                            );

                    if (expenses.compareTo(
                            BigDecimal.ZERO
                    ) < 0) {

                        sendMessage(
                                chatId,
                                "❌ Расходы не могут быть отрицательными."
                        );

                        return;
                    }

                    state.otherExpenses =
                            expenses;

                    state.step =
                            AddProductStep.CONFIRMATION;

                    sendProductConfirmation(
                            chatId,
                            state
                    );
                }


                /*
                 * CONFIRMATION
                 */

                case CONFIRMATION -> {

                    sendMessage(
                            chatId,
                            "Нажми кнопку «✅ Да» или «❌ Отмена»."
                    );
                }
            }

        } catch (NumberFormatException e) {

            sendMessage(
                    chatId,
                    "❌ Введи корректное число."
            );

        } catch (Exception e) {

            e.printStackTrace();

            addProductStates.remove(chatId);

            sendMessage(
                    chatId,
                    """
                    ❌ Ошибка:
                    
                    %s
                    """.formatted(
                            e.getMessage()
                    )
            );
        }
    }


    /*
     * =========================================================
     * CONDITION KEYBOARD
     * =========================================================
     */

    private void sendConditionKeyboard(Long chatId) {

        InlineKeyboardButton newButton =
                InlineKeyboardButton
                        .builder()
                        .text("🆕 NEW")
                        .callbackData(
                                "product_condition:NEW"
                        )
                        .build();

        InlineKeyboardButton usedButton =
                InlineKeyboardButton
                        .builder()
                        .text("♻️ USED")
                        .callbackData(
                                "product_condition:USED"
                        )
                        .build();

        InlineKeyboardButton cancel =
                InlineKeyboardButton
                        .builder()
                        .text("❌ Отмена")
                        .callbackData("cancel")
                        .build();

        InlineKeyboardMarkup keyboard =
                InlineKeyboardMarkup
                        .builder()
                        .keyboard(
                                List.of(
                                        new InlineKeyboardRow(
                                                newButton,
                                                usedButton
                                        ),
                                        new InlineKeyboardRow(
                                                cancel
                                        )
                                )
                        )
                        .build();

        SendMessage message =
                SendMessage.builder()
                        .chatId(chatId)
                        .text(
                                "📦 Выбери состояние телефона:"
                        )
                        .replyMarkup(keyboard)
                        .build();

        execute(message);
    }


    /*
     * =========================================================
     * PRODUCT CONFIRMATION
     * =========================================================
     */

    private void sendProductConfirmation(
            Long chatId,
            AddProductState state
    ) {

        BigDecimal totalPurchaseCost =
                state.purchasePrice.multiply(
                        BigDecimal.valueOf(
                                state.quantity
                        )
                );

        BigDecimal totalExpenses =
                state.shippingCost.add(
                        state.otherExpenses
                );

        BigDecimal totalCost =
                totalPurchaseCost.add(
                        totalExpenses
                );

        BigDecimal fullUnitCost =
                totalCost.divide(
                        BigDecimal.valueOf(
                                state.quantity
                        ),
                        2,
                        RoundingMode.HALF_UP
                );

        InlineKeyboardButton confirm =
                InlineKeyboardButton
                        .builder()
                        .text("✅ Да")
                        .callbackData(
                                "product_confirm"
                        )
                        .build();

        InlineKeyboardButton cancel =
                InlineKeyboardButton
                        .builder()
                        .text("❌ Отмена")
                        .callbackData(
                                "cancel"
                        )
                        .build();

        InlineKeyboardMarkup keyboard =
                InlineKeyboardMarkup
                        .builder()
                        .keyboard(
                                List.of(
                                        new InlineKeyboardRow(
                                                confirm,
                                                cancel
                                        )
                                )
                        )
                        .build();

        SendMessage message =
                SendMessage.builder()
                        .chatId(chatId)
                        .text(
                                """
                                🧾 ПРОВЕРЬ ТЕЛЕФОН
                                
                                📱 %s %s
                                💾 %s GB
                                
                                📦 Состояние:
                                %s
                                
                                📦 Количество:
                                %s шт.
                                
                                💰 Закупка:
                                %s KGS / шт.
                                
                                🚚 Доставка:
                                %s KGS
                                
                                📦 Прочие расходы:
                                %s KGS
                                
                                💵 Себестоимость 1 шт.:
                                %s KGS
                                
                                💰 Цена продажи:
                                %s KGS / шт.
                                
                                💵 Себестоимость партии:
                                %s KGS
                                
                                Добавить на склад?
                                """.formatted(
                                        state.brand,
                                        state.model,
                                        state.storage,
                                        state.condition,
                                        state.quantity,
                                        state.purchasePrice,
                                        state.shippingCost,
                                        state.otherExpenses,
                                        fullUnitCost,
                                        state.salePrice,
                                        totalCost
                                )
                        )
                        .replyMarkup(keyboard)
                        .build();

        execute(message);
    }


    /*
     * =========================================================
     * CREATE PRODUCT + STOCK + STOCK BATCH
     * =========================================================
     */

    private void createProduct(
            Long chatId,
            AddProductState state
    ) {

        try {

            /*
             * =================================================
             * PRODUCT
             * =================================================
             */

            Product product =
                    new Product();

            product.setBrand(
                    state.brand
            );

            product.setModel(
                    state.model
            );

            product.setStorage(
                    state.storage
            );

            product.setCondition(
                    state.condition
            );

            product.setPurchasePrice(
                    state.purchasePrice
            );

            product.setSalePrice(
                    state.salePrice
            );

            product.setStatus(
                    ProductStatus.IN_STOCK
            );

            Product savedProduct =
                    productRepository.save(
                            product
                    );


            /*
             * =================================================
             * CALCULATE COST
             * =================================================
             *
             * Всё уже в KGS.
             *
             * Закупка партии:
             * purchasePrice × quantity
             *
             * Полная себестоимость партии:
             * закупка + доставка + прочие расходы
             *
             * Себестоимость одной штуки:
             * полная себестоимость / quantity
             */

            BigDecimal totalPurchaseCost =
                    state.purchasePrice.multiply(
                            BigDecimal.valueOf(
                                    state.quantity
                            )
                    );

            BigDecimal totalExpenses =
                    state.shippingCost.add(
                            state.otherExpenses
                    );

            BigDecimal totalCost =
                    totalPurchaseCost.add(
                            totalExpenses
                    );

            BigDecimal fullUnitCost =
                    totalCost.divide(
                            BigDecimal.valueOf(
                                    state.quantity
                            ),
                            2,
                            RoundingMode.HALF_UP
                    );


            /*
             * =================================================
             * STOCK
             * =================================================
             */

            Stock stock =
                    stockRepository
                            .findByProductId(
                                    savedProduct.getId()
                            )
                            .orElseGet(() -> {

                                Stock newStock =
                                        new Stock();

                                newStock.setProduct(
                                        savedProduct
                                );

                                newStock.setQuantity(
                                        0
                                );

                                return newStock;
                            });

            stock.setQuantity(
                    stock.getQuantity()
                            + state.quantity
            );

            stockRepository.save(
                    stock
            );


            /*
             * =================================================
             * STOCK BATCH
             * =================================================
             */

            StockBatch stockBatch =
                    new StockBatch();

            stockBatch.setProduct(
                    savedProduct
            );

            stockBatch.setQuantity(
                    state.quantity
            );

            /*
             * ВАЖНО:
             *
             * unitCost уже в KGS.
             *
             * Никакого exchangeRate здесь нет.
             */

            stockBatch.setUnitCost(
                    fullUnitCost
            );

            stockBatch.setPurchaseDate(
                    LocalDateTime.now()
            );

            stockBatchRepository.save(
                    stockBatch
            );


            /*
             * =================================================
             * CLEAR STATE
             * =================================================
             */

            addProductStates.remove(
                    chatId
            );


            /*
             * =================================================
             * SUCCESS MESSAGE
             * =================================================
             */

            sendMessage(
                    chatId,
                    """
                    ✅ ТЕЛЕФОН ДОБАВЛЕН НА СКЛАД!
                    
                    📱 %s %s
                    💾 %s GB
                    
                    📦 Состояние:
                    %s
                    
                    📦 Количество:
                    %s шт.
                    
                    💰 Закупка:
                    %s KGS / шт.
                    
                    🚚 Доставка:
                    %s KGS
                    
                    📦 Прочие расходы:
                    %s KGS
                    
                    💵 Себестоимость 1 шт.:
                    %s KGS
                    
                    💵 Себестоимость партии:
                    %s KGS
                    
                    💰 Цена продажи:
                    %s KGS / шт.
                    
                    📦 Остаток:
                    %s шт.
                    """.formatted(
                            savedProduct.getBrand(),
                            savedProduct.getModel(),
                            savedProduct.getStorage(),
                            savedProduct.getCondition(),
                            state.quantity,
                            state.purchasePrice,
                            state.shippingCost,
                            state.otherExpenses,
                            fullUnitCost,
                            totalCost,
                            savedProduct.getSalePrice(),
                            stock.getQuantity()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            addProductStates.remove(
                    chatId
            );

            sendMessage(
                    chatId,
                    """
                    ❌ НЕ УДАЛОСЬ ДОБАВИТЬ ТЕЛЕФОН.
                    
                    Причина:
                    %s
                    """.formatted(
                            e.getMessage()
                    )
            );
        }
    }


    /*
     * =========================================================
     * ANALYTICS
     * =========================================================
     */

    private void sendAnalytics(Long chatId) {

        try {

            DashboardResponse dashboard =
                    analyticsService.getDashboard();

            sendMessage(
                    chatId,
                    """
                    📊 АНАЛИТИКА МАГАЗИНА
                    
                    💰 Выручка:
                    %s KGS
                    
                    💸 Себестоимость:
                    %s KGS
                    
                    📦 Расходы:
                    %s KGS
                    
                    💎 Чистая прибыль:
                    %s KGS
                    
                    🛒 Продано:
                    %s шт.
                    
                    📦 На складе:
                    %s шт.
                    """.formatted(
                            dashboard.getRevenue(),
                            dashboard.getCost(),
                            dashboard.getExpenses(),
                            dashboard.getNetProfit(),
                            dashboard.getSoldItems(),
                            dashboard.getStockItems()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendMessage(
                    chatId,
                    "❌ Не удалось получить аналитику."
            );
        }
    }


    /*
     * =========================================================
     * STOCK
     * =========================================================
     */

    private void sendStock(Long chatId) {

        try {

            List<ProductAnalyticsResponse> products =
                    analyticsService.getProductsInStock();

            if (products.isEmpty()) {

                sendMessage(
                        chatId,
                        "📦 На складе пока нет товаров."
                );

                return;
            }

            StringBuilder message =
                    new StringBuilder(
                            "📦 ОСТАТКИ\n\n"
                    );

            for (
                    ProductAnalyticsResponse product
                    : products
            ) {

                message.append("📱 ")
                        .append(product.getBrand())
                        .append(" ")
                        .append(product.getModel())
                        .append("\n");

                message.append("💾 ")
                        .append(product.getStorage())
                        .append(" GB\n");

                message.append("📦 Остаток: ")
                        .append(product.getStock())
                        .append(" шт.\n");

                message.append("⏳ Запас: ")
                        .append(product.getDaysOfStock())
                        .append(" дней\n");

                message.append("🎯 ")
                        .append(product.getRecommendation())
                        .append("\n\n");
            }

            sendMessage(
                    chatId,
                    message.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendMessage(
                    chatId,
                    "❌ Не удалось получить остатки."
            );
        }
    }


    /*
     * =========================================================
     * PRODUCTS
     * =========================================================
     */

    private void sendProducts(Long chatId) {

        try {

            List<ProductAnalyticsResponse> products =
                    analyticsService.getProductsAnalytics();

            if (products.isEmpty()) {

                sendMessage(
                        chatId,
                        "📱 Товаров пока нет."
                );

                return;
            }

            StringBuilder message =
                    new StringBuilder(
                            "📱 ТОВАРЫ\n\n"
                    );

            for (
                    ProductAnalyticsResponse product
                    : products
            ) {

                message.append("📱 ")
                        .append(product.getBrand())
                        .append(" ")
                        .append(product.getModel())
                        .append("\n");

                message.append("💾 ")
                        .append(product.getStorage())
                        .append(" GB\n");

                message.append("📦 Остаток: ")
                        .append(product.getStock())
                        .append("\n");

                message.append("🛒 Продано: ")
                        .append(product.getSold())
                        .append("\n");

                message.append("💰 Выручка: ")
                        .append(product.getRevenue())
                        .append(" KGS\n");

                message.append("💎 Прибыль: ")
                        .append(product.getProfit())
                        .append(" KGS\n");

                message.append("📊 Продаж/день: ")
                        .append(product.getSalesPerDay())
                        .append("\n");

                message.append("⏳ Дней запаса: ")
                        .append(product.getDaysOfStock())
                        .append("\n");

                message.append("🎯 ")
                        .append(product.getRecommendation())
                        .append("\n\n");
            }

            sendMessage(
                    chatId,
                    message.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendMessage(
                    chatId,
                    "❌ Не удалось получить товары."
            );
        }
    }


    /*
     * =========================================================
     * PROFIT
     * =========================================================
     */

    private void sendProfit(Long chatId) {

        try {

            DashboardResponse dashboard =
                    analyticsService.getDashboard();

            sendMessage(
                    chatId,
                    """
                    📈 ПРИБЫЛЬ
                    
                    💵 Выручка:
                    %s KGS
                    
                    💸 Себестоимость:
                    %s KGS
                    
                    📦 Расходы:
                    %s KGS
                    
                    💎 Чистая прибыль:
                    %s KGS
                    """.formatted(
                            dashboard.getRevenue(),
                            dashboard.getCost(),
                            dashboard.getExpenses(),
                            dashboard.getNetProfit()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendMessage(
                    chatId,
                    "❌ Не удалось получить прибыль."
            );
        }
    }


    /*
     * =========================================================
     * SALE
     * =========================================================
     */

    private void startSale(Long chatId) {

        clearStates(chatId);

        List<Stock> stocks =
                stockRepository
                        .findByQuantityGreaterThan(0);

        if (stocks.isEmpty()) {

            sendMessage(
                    chatId,
                    """
                    📦 На складе нет товаров.
                    
                    Сначала добавь телефон:
                    📱 Добавить телефон
                    """
            );

            return;
        }

        List<Product> products =
                stocks.stream()
                        .map(Stock::getProduct)
                        .toList();

        SaleState state =
                new SaleState();

        saleStates.put(
                chatId,
                state
        );

        sendProductSelection(
                chatId,
                products,
                "sale_product:",
                """
                💰 НОВАЯ ПРОДАЖА
                
                📱 Выбери телефон:
                """
        );
    }


    /*
     * =========================================================
     * SALE STEPS
     * =========================================================
     */

    private void handleSaleStep(
            Long chatId,
            String text
    ) {

        SaleState state =
                saleStates.get(chatId);

        if (state == null) {
            return;
        }

        try {

            switch (state.step) {

                case QUANTITY -> {

                    int quantity =
                            Integer.parseInt(
                                    text.trim()
                            );

                    if (quantity <= 0) {

                        sendMessage(
                                chatId,
                                "❌ Количество должно быть больше 0."
                        );

                        return;
                    }

                    Stock stock =
                            stockRepository
                                    .findByProductId(
                                            state.productId
                                    )
                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "Stock not found"
                                            )
                                    );

                    if (stock.getQuantity()
                            < quantity) {

                        sendMessage(
                                chatId,
                                """
                                ❌ Недостаточно товара.
                                
                                📦 На складе:
                                %s шт.
                                
                                Ты указал:
                                %s шт.
                                """.formatted(
                                        stock.getQuantity(),
                                        quantity
                                )
                        );

                        return;
                    }

                    state.quantity =
                            quantity;

                    state.step =
                            SaleStep.UNIT_PRICE;

                    sendMessage(
                            chatId,
                            """
                            💰 Введи цену продажи
                            за одну штуку в KGS.
                            
                            Например:
                            65000
                            """
                    );
                }


                case UNIT_PRICE -> {

                    BigDecimal price =
                            new BigDecimal(
                                    text.trim()
                            );

                    if (price.compareTo(
                            BigDecimal.ZERO
                    ) <= 0) {

                        sendMessage(
                                chatId,
                                "❌ Цена должна быть больше 0."
                        );

                        return;
                    }

                    state.unitPrice =
                            price;

                    state.step =
                            SaleStep.EXPENSES;

                    sendMessage(
                            chatId,
                            """
                            📦 Введи расходы по продаже.
                            
                            Если расходов нет:
                            0
                            
                            Например:
                            500
                            """
                    );
                }

                case EXPENSES -> {

                    BigDecimal expenses =
                            new BigDecimal(
                                    text.trim()
                            );

                    if (expenses.compareTo(
                            BigDecimal.ZERO
                    ) < 0) {

                        sendMessage(
                                chatId,
                                "❌ Расходы не могут быть отрицательными."
                        );

                        return;
                    }

                    state.expenses =
                            expenses;

                    state.step =
                            SaleStep.CONFIRMATION;

                    sendSaleConfirmation(
                            chatId,
                            state
                    );
                }


                case CONFIRMATION -> {

                    sendMessage(
                            chatId,
                            "Нажми «✅ Да» или «❌ Отмена»."
                    );
                }
            }

        } catch (NumberFormatException e) {

            sendMessage(
                    chatId,
                    "❌ Введи корректное число."
            );

        } catch (Exception e) {

            e.printStackTrace();

            saleStates.remove(chatId);

            sendMessage(
                    chatId,
                    """
                    ❌ Ошибка:
                    
                    %s
                    """.formatted(
                            e.getMessage()
                    )
            );
        }
    }


    /*
     * =========================================================
     * SALE CONFIRMATION
     * =========================================================
     */

    private void sendSaleConfirmation(
            Long chatId,
            SaleState state
    ) {

        BigDecimal total =
                state.unitPrice.multiply(
                        BigDecimal.valueOf(
                                state.quantity
                        )
                );

        Product product =
                productRepository.findById(
                        state.productId
                ).orElse(null);

        String productName =
                product == null
                        ? "Товар"
                        : product.getBrand()
                          + " "
                          + product.getModel()
                          + " "
                          + product.getStorage()
                          + " GB";

        InlineKeyboardButton confirm =
                InlineKeyboardButton
                        .builder()
                        .text("✅ Да")
                        .callbackData(
                                "sale_confirm"
                        )
                        .build();

        InlineKeyboardButton cancel =
                InlineKeyboardButton
                        .builder()
                        .text("❌ Отмена")
                        .callbackData(
                                "cancel"
                        )
                        .build();

        InlineKeyboardMarkup keyboard =
                InlineKeyboardMarkup
                        .builder()
                        .keyboard(
                                List.of(
                                        new InlineKeyboardRow(
                                                confirm,
                                                cancel
                                        )
                                )
                        )
                        .build();

        SendMessage message =
                SendMessage.builder()
                        .chatId(chatId)
                        .text(
                                """
                                🧾 ПРОВЕРЬ ПРОДАЖУ
                                
                                📱 Товар:
                                %s
                                
                                📦 Количество:
                                %s шт.
                                
                                💰 Цена продажи:
                                %s KGS / шт.
                                
                                💵 Сумма:
                                %s KGS
                                
                                📦 Расходы:
                                %s KGS
                                
                                Подтвердить продажу?
                                """.formatted(
                                        productName,
                                        state.quantity,
                                        state.unitPrice,
                                        total,
                                        state.expenses
                                )
                        )
                        .replyMarkup(keyboard)
                        .build();

        execute(message);
    }


    /*
     * =========================================================
     * CREATE SALE
     * =========================================================
     */

    private void createSale(
            Long chatId,
            SaleState state
    ) {

        try {

            SaleItemRequest item =
                    new SaleItemRequest();

            item.setProductId(
                    state.productId
            );

            item.setQuantity(
                    state.quantity
            );

            item.setUnitPrice(
                    state.unitPrice
            );


            SaleRequest request =
                    new SaleRequest();

            request.setSaleDate(
                    LocalDateTime.now()
            );

            request.setCurrency(
                    "KGS"
            );

            request.setExpenses(
                    state.expenses
            );

            request.setItems(
                    List.of(item)
            );


            saleService.create(
                    request
            );


            BigDecimal total =
                    state.unitPrice.multiply(
                            BigDecimal.valueOf(
                                    state.quantity
                            )
                    );

            saleStates.remove(chatId);

            sendMessage(
                    chatId,
                    """
                    ✅ ПРОДАЖА СОЗДАНА
                    
                    📦 Количество:
                    %s шт.
                    
                    💰 Сумма:
                    %s KGS
                    
                    📦 Остаток обновлён.
                    💎 Прибыль рассчитана.
                    """.formatted(
                            state.quantity,
                            total
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            saleStates.remove(chatId);

            sendMessage(
                    chatId,
                    """
                    ❌ ПРОДАЖА НЕ СОЗДАНА
                    
                    Причина:
                    %s
                    """.formatted(
                            e.getMessage()
                    )
            );
        }
    }


    /*
     * =========================================================
     * DELETE FROM STOCK
     * =========================================================
     */

    private void startDeleteFromStock(Long chatId) {

        clearStates(chatId);

        List<Stock> stocks =
                stockRepository
                        .findByQuantityGreaterThan(0);

        if (stocks.isEmpty()) {

            sendMessage(
                    chatId,
                    "📦 На складе нет товаров."
            );

            return;
        }

        List<Product> products =
                stocks.stream()
                        .map(Stock::getProduct)
                        .toList();

        DeleteState state =
                new DeleteState();

        deleteStates.put(
                chatId,
                state
        );

        sendProductSelection(
                chatId,
                products,
                "delete_product:",
                """
                🗑 УДАЛЕНИЕ СО СКЛАДА
                
                📱 Выбери телефон:
                """
        );
    }


    private void handleDeleteStep(
            Long chatId,
            String text
    ) {

        DeleteState state =
                deleteStates.get(chatId);

        if (state == null) {
            return;
        }

        try {

            int quantity =
                    Integer.parseInt(
                            text.trim()
                    );

            if (quantity <= 0) {

                sendMessage(
                        chatId,
                        "❌ Количество должно быть больше 0."
                );

                return;
            }

            Stock stock =
                    stockRepository
                            .findByProductId(
                                    state.productId
                            )
                            .orElse(null);

            if (stock == null) {

                deleteStates.remove(chatId);

                sendMessage(
                        chatId,
                        "❌ Этот товар отсутствует на складе."
                );

                return;
            }

            if (stock.getQuantity()
                    < quantity) {

                sendMessage(
                        chatId,
                        """
                        ❌ Недостаточно товара.
                        
                        📦 На складе:
                        %s шт.
                        
                        Ты указал:
                        %s шт.
                        """.formatted(
                                stock.getQuantity(),
                                quantity
                        )
                );

                return;
            }

            state.quantity =
                    quantity;

            Product product =
                    productRepository.findById(
                            state.productId
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Product not found"
                            )
                    );

            int remaining =
                    stock.getQuantity()
                            - quantity;

            InlineKeyboardButton confirm =
                    InlineKeyboardButton
                            .builder()
                            .text("✅ Да")
                            .callbackData(
                                    "delete_confirm"
                            )
                            .build();

            InlineKeyboardButton cancel =
                    InlineKeyboardButton
                            .builder()
                            .text("❌ Отмена")
                            .callbackData(
                                    "cancel"
                            )
                            .build();

            InlineKeyboardMarkup keyboard =
                    InlineKeyboardMarkup
                            .builder()
                            .keyboard(
                                    List.of(
                                            new InlineKeyboardRow(
                                                    confirm,
                                                    cancel
                                            )
                                    )
                            )
                            .build();

            SendMessage message =
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(
                                    """
                                    ⚠️ ПОДТВЕРЖДЕНИЕ
                                    
                                    📱 %s %s
                                    💾 %s GB
                                    
                                    📦 Было:
                                    %s шт.
                                    
                                    🗑 Удалить:
                                    %s шт.
                                    
                                    📦 Останется:
                                    %s шт.
                                    
                                    Подтвердить удаление?
                                    """.formatted(
                                            product.getBrand(),
                                            product.getModel(),
                                            product.getStorage(),
                                            stock.getQuantity(),
                                            quantity,
                                            remaining
                                    )
                            )
                            .replyMarkup(keyboard)
                            .build();

            execute(message);

        } catch (NumberFormatException e) {

            sendMessage(
                    chatId,
                    "❌ Введи обычное число. Например: 1"
            );

        } catch (Exception e) {

            e.printStackTrace();

            deleteStates.remove(chatId);

            sendMessage(
                    chatId,
                    """
                    ❌ Ошибка:
                    
                    %s
                    """.formatted(
                            e.getMessage()
                    )
            );
        }
    }


    private void createDelete(Long chatId) {

        DeleteState state =
                deleteStates.get(chatId);

        if (state == null) {
            return;
        }

        try {

            Product product =
                    productRepository.findById(
                            state.productId
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Product not found"
                            )
                    );

            stockService.removeFromStock(
                    state.productId,
                    state.quantity
            );

            deleteStates.remove(chatId);

            sendMessage(
                    chatId,
                    """
                    ✅ ТЕЛЕФОН УДАЛЁН СО СКЛАДА
                    
                    📱 %s %s
                    💾 %s GB
                    
                    🗑 Удалено:
                    %s шт.
                    
                    📦 Склад обновлён.
                    """.formatted(
                            product.getBrand(),
                            product.getModel(),
                            product.getStorage(),
                            state.quantity
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            deleteStates.remove(chatId);

            sendMessage(
                    chatId,
                    """
                    ❌ НЕ УДАЛОСЬ УДАЛИТЬ ТОВАР
                    
                    Причина:
                    %s
                    """.formatted(
                            e.getMessage()
                    )
            );
        }
    }


    /*
     * =========================================================
     * CALLBACK HANDLER
     * =========================================================
     */

    private void handleCallback(Update update) {

        CallbackQuery callback =
                update.getCallbackQuery();

        String data =
                callback.getData();

        Long chatId =
                callback.getMessage()
                        .getChatId();

        answerCallback(
                callback.getId()
        );


        /*
         * CANCEL
         */

        if (data.equals("cancel")) {

            clearStates(chatId);

            sendMessage(
                    chatId,
                    "❌ Операция отменена."
            );

            return;
        }

        if (data.equals(
                "analytics_reset_confirm"
        )) {

            try {

                analyticsResetService.reset();

                sendMessage(
                        chatId,
                        """
                        ✅ АНАЛИТИКА СБРОШЕНА
                         
                        💰 Выручка: 0
                        💎 Прибыль: 0
                        🛒 Продажи: 0
                         
                        📦 Склад сохранён.
                        """
                );

            } catch (Exception e) {

                e.printStackTrace();

                sendMessage(
                        chatId,
                        """
                        ❌ Не удалось сбросить аналитику.
                        
                        Причина:
                        %s
                        """.formatted(
                                e.getMessage()
                        )
                );
            }

            return;
        }


        /*
         * PRODUCT CONDITION
         */

        if (data.startsWith(
                "product_condition:"
        )) {

            AddProductState state =
                    addProductStates.get(chatId);

            if (state == null) {
                return;
            }

            String conditionValue =
                    data.substring(
                            "product_condition:"
                                    .length()
                    );

            state.condition =
                    ProductCondition.valueOf(
                            conditionValue
                    );

            state.step =
                    AddProductStep.PURCHASE_PRICE;

            sendMessage(
                    chatId,
                    """
                    💰 Введи цену закупки
                    за одну штуку:
                    
                    Например:
                    50000
                    """
            );

            return;
        }


        /*
         * PRODUCT CONFIRM
         */

        if (data.equals(
                "product_confirm"
        )) {

            AddProductState state =
                    addProductStates.get(chatId);

            if (state == null) {
                return;
            }

            createProduct(
                    chatId,
                    state
            );

            return;
        }


        /*
         * SALE PRODUCT
         */

        if (data.startsWith(
                "sale_product:"
        )) {

            Long productId =
                    Long.parseLong(
                            data.substring(
                                    "sale_product:"
                                            .length()
                            )
                    );

            Product product =
                    productRepository.findById(
                            productId
                    ).orElse(null);

            if (product == null) {

                sendMessage(
                        chatId,
                        "❌ Товар не найден."
                );

                return;
            }

            Stock stock =
                    stockRepository
                            .findByProductId(
                                    productId
                            )
                            .orElse(null);

            if (stock == null
                    || stock.getQuantity() <= 0) {

                sendMessage(
                        chatId,
                        "❌ Этого товара сейчас нет на складе."
                );

                return;
            }

            SaleState state =
                    saleStates.get(chatId);

            if (state == null) {

                state =
                        new SaleState();

                saleStates.put(
                        chatId,
                        state
                );
            }

            state.productId =
                    productId;

            state.step =
                    SaleStep.QUANTITY;

            sendMessage(
                    chatId,
                    """
                    💰 ПРОДАЖА
                    
                    📱 %s %s
                    💾 %s GB
                    
                    📦 На складе:
                    %s шт.
                    
                    Введи количество:
                    """.formatted(
                            product.getBrand(),
                            product.getModel(),
                            product.getStorage(),
                            stock.getQuantity()
                    )
            );

            return;
        }


        /*
         * SALE CONFIRM
         */

        if (data.equals(
                "sale_confirm"
        )) {

            SaleState state =
                    saleStates.get(chatId);

            if (state == null) {
                return;
            }

            createSale(
                    chatId,
                    state
            );

            return;
        }


        /*
         * DELETE PRODUCT
         */

        if (data.startsWith(
                "delete_product:"
        )) {

            Long productId =
                    Long.parseLong(
                            data.substring(
                                    "delete_product:"
                                            .length()
                            )
                    );

            Product product =
                    productRepository.findById(
                            productId
                    ).orElse(null);

            if (product == null) {

                sendMessage(
                        chatId,
                        "❌ Товар не найден."
                );

                return;
            }

            Stock stock =
                    stockRepository
                            .findByProductId(
                                    productId
                            )
                            .orElse(null);

            if (stock == null
                    || stock.getQuantity() <= 0) {

                sendMessage(
                        chatId,
                        "📦 Этого товара сейчас нет на складе."
                );

                return;
            }

            DeleteState state =
                    deleteStates.get(chatId);

            if (state == null) {

                state =
                        new DeleteState();

                deleteStates.put(
                        chatId,
                        state
                );
            }

            state.productId =
                    productId;

            sendMessage(
                    chatId,
                    """
                    🗑 УДАЛЕНИЕ СО СКЛАДА
                    
                    📱 %s %s
                    💾 %s GB
                    
                    📦 Сейчас на складе:
                    %s шт.
                    
                    Введи количество:
                    """.formatted(
                            product.getBrand(),
                            product.getModel(),
                            product.getStorage(),
                            stock.getQuantity()
                    )
            );

            return;
        }


        /*
         * DELETE CONFIRM
         */

        if (data.equals(
                "delete_confirm"
        )) {

            createDelete(chatId);
        }
    }


    /*
     * =========================================================
     * PRODUCT INLINE KEYBOARD
     * =========================================================
     */

    private void sendProductSelection(
            Long chatId,
            List<Product> products,
            String callbackPrefix,
            String title
    ) {

        List<List<InlineKeyboardButton>> rows =
                new ArrayList<>();

        for (Product product : products) {

            String productName =
                    "📱 "
                            + product.getBrand()
                            + " "
                            + product.getModel()
                            + " • "
                            + product.getStorage()
                            + " GB";

            InlineKeyboardButton button =
                    InlineKeyboardButton
                            .builder()
                            .text(productName)
                            .callbackData(
                                    callbackPrefix
                                            + product.getId()
                            )
                            .build();

            rows.add(
                    List.of(button)
            );
        }

        InlineKeyboardButton cancel =
                InlineKeyboardButton
                        .builder()
                        .text("❌ Отмена")
                        .callbackData("cancel")
                        .build();

        rows.add(
                List.of(cancel)
        );

        InlineKeyboardMarkup keyboard =
                InlineKeyboardMarkup
                        .builder()
                        .keyboard(
                                rows.stream()
                                        .map(
                                                InlineKeyboardRow::new
                                        )
                                        .toList()
                        )
                        .build();

        SendMessage message =
                SendMessage.builder()
                        .chatId(chatId)
                        .text(title)
                        .replyMarkup(keyboard)
                        .build();

        execute(message);
    }


    /*
     * =========================================================
     * AI
     * =========================================================
     */

    private void handleAi(
            Long chatId,
            String text
    ) {

        try {

            String[] parts =
                    text.split("\\s+");

            if (parts.length != 3) {

                sendMessage(
                        chatId,
                        """
                        🤖 AI-СОВЕТНИК
                        
                        Используй:
                        
                        /ai 12.5 100000
                        """
                );

                return;
            }

            BigDecimal exchangeRate =
                    new BigDecimal(
                            parts[1]
                    );

            BigDecimal budget =
                    new BigDecimal(
                            parts[2]
                    );

            AiPurchaseAdviceRequest request =
                    new AiPurchaseAdviceRequest();

            request.setExchangeRate(
                    exchangeRate
            );

            request.setBudget(
                    budget
            );

            AiPurchaseAdviceResponse response =
                    aiPurchaseAdvisorService.getAdvice(
                            request
                    );

            sendMessage(
                    chatId,
                    """
                    🤖 AI-СОВЕТНИК
                    
                    💰 Бюджет:
                    %s KGS
                    
                    💸 Использовано:
                    %s KGS
                    
                    💵 Осталось:
                    %s KGS
                    
                    📈 Ожидаемая выручка:
                    %s KGS
                    
                    💎 Ожидаемая прибыль:
                    %s KGS
                    
                    ━━━━━━━━━━━━━━━
                    
                    🧠 РЕКОМЕНДАЦИЯ:
                    
                    %s
                    """.formatted(
                            response.getBudget(),
                            response.getUsedBudget(),
                            response.getRemainingBudget(),
                            response.getExpectedRevenue(),
                            response.getExpectedProfit(),
                            response.getAdvice()
                    )
            );

        } catch (NumberFormatException e) {

            sendMessage(
                    chatId,
                    """
                    ❌ Неверный формат.
                    
                    Пример:
                    
                    /ai 12.5 100000
                    """
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendMessage(
                    chatId,
                    """
                    ❌ AI не смог дать рекомендацию.
                    
                    Проверь Ollama и Qwen.
                    """
            );
        }
    }


    /*
     * =========================================================
     * CALLBACK ANSWER
     * =========================================================
     */

    private void answerCallback(
            String callbackQueryId
    ) {

        try {

            telegramClient.execute(
                    AnswerCallbackQuery
                            .builder()
                            .callbackQueryId(
                                    callbackQueryId
                            )
                            .build()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * SEND MESSAGE
     * =========================================================
     */

    private void sendMessage(
            Long chatId,
            String text
    ) {

        SendMessage message =
                SendMessage.builder()
                        .chatId(chatId)
                        .text(text)
                        .build();

        execute(message);
    }


    /*
     * =========================================================
     * EXECUTE
     * =========================================================
     */

    private void execute(
            SendMessage message
    ) {

        try {

            telegramClient.execute(
                    message
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */

    private boolean isCommand(
            String text,
            String... commands
    ) {

        for (String command : commands) {

            if (text.equalsIgnoreCase(command)) {
                return true;
            }
        }

        return false;
    }


    private void clearStates(Long chatId) {

        saleStates.remove(chatId);

        deleteStates.remove(chatId);

        addProductStates.remove(chatId);
    }


    /*
     * =========================================================
     * SALE STATE
     * =========================================================
     */

    private static class SaleState {

        private SaleStep step;

        private Long productId;

        private Integer quantity;

        private BigDecimal unitPrice;

        private BigDecimal expenses;
    }


    private enum SaleStep {

        QUANTITY,
        UNIT_PRICE,
        EXPENSES,
        CONFIRMATION
    }


    /*
     * =========================================================
     * DELETE STATE
     * =========================================================
     */

    private static class DeleteState {

        private Long productId;

        private Integer quantity;
    }


    /*
     * =========================================================
     * ADD PRODUCT STATE
     * =========================================================
     */

    private static class AddProductState {

        private AddProductStep step;

        private String brand;

        private String model;

        private Integer storage;

        private ProductCondition condition;

        private BigDecimal purchasePrice;

        private BigDecimal salePrice;

        private Integer quantity;

        private BigDecimal shippingCost;

        private BigDecimal otherExpenses;
    }


    private enum AddProductStep {

        BRAND,
        MODEL,
        STORAGE,
        CONDITION,
        PURCHASE_PRICE,
        SALE_PRICE,
        QUANTITY,
        SHIPPING_COST,
        OTHER_EXPENSES,
        CONFIRMATION
    }
}