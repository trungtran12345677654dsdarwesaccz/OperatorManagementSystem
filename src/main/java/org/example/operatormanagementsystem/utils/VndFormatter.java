package org.example.operatormanagementsystem.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class VndFormatter {
    
    private static final NumberFormat VND_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        return VND_FORMAT.format(amount);
    }
    
    public static String formatWithoutSymbol(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return String.format("%,.0f", amount);
    }
} 