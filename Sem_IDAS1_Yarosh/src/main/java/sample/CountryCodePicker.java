package sample;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CountryCodePicker {

    private CountryCodePicker() {
    }

    private static final ObservableList<String> COUNTRY_CODES = Stream.of(Locale.getISOCountries())
            .map(locales -> new Locale("", locales).getCountry())
            .filter(countryCode -> getPhoneCountyCode(countryCode) != 0)
            .map(countryCode -> String.format("%s +%d", countryCode, getPhoneCountyCode(countryCode)))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    //Запускаем стрим на все доступные страны
    //возвращаем код страны
    //нужна проверка, потому что если нет в базе присоеденённой библиотеки кода страны, то вернёт 0
    //собираем строку вида АБ +123
    //возвращаем нужный объект

    public static ObservableList<String> getCountryCodes() {
        return COUNTRY_CODES;
    }

    private static int getPhoneCountyCode(final String countryCode) {
        return PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryCode);
    }

    public static String getISOCountry(final String telephone) {
        final List<String> list = COUNTRY_CODES.stream().filter(str -> str.substring(0, 2).equals(telephone.substring(0, 2))).collect(Collectors.toList());
        return list.isEmpty() ? COUNTRY_CODES.get(0) : list.get(0);
    }

    @NotNull
    public static String getPhoneNumber(@NotNull final String telephone) {
        return telephone.substring(getISOCountry(telephone).length());
    }
}
