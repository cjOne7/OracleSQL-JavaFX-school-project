package sample;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CountryCodePicker {

    public static ObservableList<String> getCountryCodes() {
        return Stream.of(Locale.getISOCountries())
                .map(locales -> new Locale("", locales).getCountry())
                .filter(countryCode -> getPhoneCountyCode(countryCode) != 0)
                .map(countryCode -> String.format("%s +%d", countryCode, getPhoneCountyCode(countryCode)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        //Запускаем стрим на все доступные страны
        //возвращаем код страны
        //нужна проверка, потому что если нет в базе присоеденённой библиотеки кода страны, то вернёт 0
        //собираем строку вида АБ +123
        //возвращаем нужный объект
    }

    private static int getPhoneCountyCode(final String countryCode) {
        return PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryCode);
    }
}
