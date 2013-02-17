<?php

namespace ApiModule;

class ApiJSON {

    /** @var stdClass */
    private $json;

    private function __construct($json) {
        $this->json = $json;
    }
    /**
     * @param String
     */

    public static function getEmptyInstance() {
        $instance = new ApiJSON(new \stdClass());
        return $instance;
    }

    public static function load($jsonString) {
        $instance = new ApiJSON(json_decode($jsonString));
        return $instance;
    }

    public function get($key) {
        if (isset($this->json->$key))
            return $this->json->$key;
        else
            return NULL;
    }

    public function set($key, $value) {
        $this->json->$key = $value;
    }

    public function remove($key) {
        unset($this->json->$key);
    }
}
