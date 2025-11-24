package agents.tools;

import com.google.adk.tools.Annotations;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HealthTools {

    /**
     * Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor Equation.
     * <p>
     * BMR is the number of calories your body needs to accomplish its most basic (basal) life-sustaining functions.
     * <p>
     */
    public static Map<String, Object> calculateBmr(
            @Annotations.Schema(name = "sex", description = "Sex: male/female (m/f also accepted). Korean: 남성/여성") String sex,
            @Annotations.Schema(name = "age", description = "Age in years") int age,
            @Annotations.Schema(name = "height_cm", description = "Height in centimeters") double heightCm,
            @Annotations.Schema(name = "weight_kg", description = "Weight in kilograms") double weightKg
    ) {
        String normalizedSex = normalizeSex(sex);
        if (normalizedSex == null) {
            throw new IllegalArgumentException("Invalid sex. Use male/female (or m/f, 남성/여성).");
        }

        double bmr = mifflinStJeor(normalizedSex, age, heightCm, weightKg);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("sex", normalizedSex);
        inputs.put("age", age);
        inputs.put("height_cm", heightCm);
        inputs.put("weight_kg", weightKg);

        Map<String, Object> result = new HashMap<>();
        result.put("bmr", round(bmr, 2));
        result.put("inputs", inputs);
        result.put("explanation", buildExplanation(normalizedSex, age, heightCm, weightKg, bmr));
        return result;
    }

    private static String normalizeSex(String sexRaw) {
        if (sexRaw == null) return null;
        String s = sexRaw.trim().toLowerCase(Locale.ROOT);
        switch (s) {
            case "m":
            case "male":
            case "남자":
            case "남성":
                return "male";
            case "f":
            case "female":
            case "여자":
            case "여성":
                return "female";
            default:
                return null;
        }
    }

    private static double mifflinStJeor(String sex, int age, double heightCm, double weightKg) {
        double base = 10.0 * weightKg + 6.25 * heightCm - 5.0 * age;
        return base + ("male".equals(sex) ? 5.0 : -161.0);
    }

    private static double harrisBenedict(String sex, int age, double heightCm, double weightKg) {
        if ("male".equals(sex)) {
            return 88.362 + 13.397 * weightKg + 4.799 * heightCm - 5.677 * age;
        } else {
            return 447.593 + 9.247 * weightKg + 3.098 * heightCm - 4.330 * age;
        }
    }

    private static double round(double value, int decimals) {
        double p = Math.pow(10, decimals);
        return Math.round(value * p) / p;
    }

    private static String buildExplanation(String sex, int age, double heightCm, double weightKg, double bmr) {
        return String.format(Locale.ROOT,
                "입력값: 성별=%s, 나이=%d세, 키=%.1fcm, 체중=%.1fkg\n기초대사량(BMR): %.2f kcal/일",
                "male".equals(sex) ? "남성" : "여성",
                age, heightCm, weightKg, bmr);
    }
}

