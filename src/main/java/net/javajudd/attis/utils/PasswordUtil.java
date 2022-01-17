package net.javajudd.attis.utils;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

public class PasswordUtil {

    static CharacterRule digits = new CharacterRule(EnglishCharacterData.Digit, 2);
    static CharacterRule upper = new CharacterRule(EnglishCharacterData.UpperCase, 4);
    static CharacterRule lower = new CharacterRule(EnglishCharacterData.LowerCase, 4);
    static CharacterRule special = new CharacterRule(new CharacterData() {
        public String getErrorCode() {
            return "INSUFFICIENT_SPECIAL";
        }

        public String getCharacters() {
            return "!@#$%";
        }
    },2);

    static PasswordGenerator passwordGenerator = new PasswordGenerator();

    public static String generatePassword() {
        return passwordGenerator.generatePassword(12, digits, lower, upper, special);
    }
}
