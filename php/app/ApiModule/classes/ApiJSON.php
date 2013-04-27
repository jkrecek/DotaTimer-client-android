<?php

namespace ApiModule;

class ApiJSON {

    /** @var stdClass */
    private $json;

    private function __construct($json) {
        $this->json = $json;
    }

    public static function getEmptyInstance() {
        $instance = new ApiJSON(new \stdClass());
        return $instance;
    }

    /**
     * @param String
     */

    public static function load($jsonString) {
        $instance = new ApiJSON(json_decode($jsonString));
        return $instance;
    }

    public function get($key) {
        if (isset($this->json->$key))
            return $this->json->$key;
        else
            throw new BadRequestException("Request does not contain key '".$key."'.");
    }

    public function set($key, $value) {
        $this->json->$key = $value;
    }

    public function remove($key) {
        unset($this->json->$key);
    }

    public function toJson() {
        return $this->json;
    }
}
