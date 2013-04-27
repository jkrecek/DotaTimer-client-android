<?php

namespace ApiModule;

class Method {
    public static function generateString($length = 50) {
        $source = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        $string = "";
        $chars_count = strlen($source);
        for($i = 0; $i < $length; ++$i)
            $string .= $source[rand(0, $chars_count-1)];

        return $string;
    }

    public static function containtsOnlyInt(array $arr) {
        foreach ($arr as $val) {
            if (is_array($val)) {
                if (!self::containtsIntOnly($val))
                    return false;
            } else {
                if (!is_int($val))
                    return false;
            }
        }

        return true;
    }

    public static function lastWord($string, $strip = false) {
        $last_word = substr($string, strrpos($string, " ") + 1);
        if ($strip) {
            return strtr($last_word, array(
            '\'' => '',
            '\"' => '',
            ));
        } else
            return $last_word;
    }
}

