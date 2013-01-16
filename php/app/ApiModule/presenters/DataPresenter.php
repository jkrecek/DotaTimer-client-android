<?php
namespace ApiModule;

use \Nette\Application\ForbiddenRequestException,
    \Nette\Application\BadRequestException,
    \stdClass as JSONObject;

class DataPresenter extends \BasePresenter {
    public $json;

    private $id;
    private $nick;
    private $password;

    public static $instance;

    public function startup() {
        self::$instance = $this;
        parent::startup();
    }

    public function actionRead($format, $data, $id, $query = array(), $associations = array()) {
        try {
            $this->setInitial($id, $query);
            $this->loadLocalJSON();
            $this->checkPassword();
            ApiResponser::returnOK();
        } catch (ForbiddenRequestException $e) {
            ApiResponser::returnForbidden($e->getMessage());
        } catch (BadRequestException $e) {
            ApiResponser::returnBadRequest($e->getMessage());
        }
    }

    public function actionCreate($format, $data, $id, $query = array(), $associations = array()) {
        try {
            $this->setInitial($id, $data);

            $this->loadLocalJSON();
            if ($this->json == NULL)
                $this->json = $this->createBaseJson();
            else
                $this->checkPassword();

            $this->addDataToJson($this->json, self::createUserData($this->nick, "", ""));
            $this->saveJson();

            ApiResponser::returnCreated();
        } catch (ForbiddenRequestException $e) {
            ApiResponser::returnForbidden($e->getMessage());
        } catch (BadRequestException $e) {
            ApiResponser::returnBadRequest($e->getMessage());
        }
    }

    public function actionUpdate($format, $data, $id, $query = array(), $associations = array()) {
        $this->setInitial($id, $data);
    }


    private function setInitial($id, $request) {
        if (is_array($request)) {
            $this->id = $id;

            if (!array_key_exists(Tags::$TAG_CHANNEL_PASS, $request))
                throw new ForbiddenRequestException("Array: Password is required.");
            $this->password = $request[Tags::$TAG_CHANNEL_PASS];

            if (!array_key_exists(Tags::$TAG_NICK, $request))
                throw new BadRequestException("Array: Author nick is required.");
            $this->nick = $request[Tags::$TAG_NICK];

        } elseif(is_string($request) && is_object($data = json_decode($request))) {
            $fieldId = Tags::$TAG_CHANNEL_NAME;
            if (!isset($data->$fieldId))
                throw new ForbiddenRequestException("JSON: Id is required.");
            $this->id = $data->$fieldId;

            $fieldPass = Tags::$TAG_CHANNEL_PASS;
            if (!isset($data->$fieldPass))
                throw new ForbiddenRequestException("JSON: Password is required.");
            $this->password = $data->$fieldPass;

            $fieldNick = Tags::$TAG_NICK;
            if (!isset($data->$fieldNick))
                throw new BadRequestException("JSON: Author nick is required.");
            $this->nick = $data->$fieldNick;
        }
        else
            throw new BadRequestException("Bad values supplied.");
    }

    private function loadLocalJSON() {
        if ($this->json == NULL) {
            $filename =  $this->getRouteToJSON($this->id);
            if (file_exists($filename)) {
                $fileContent = file_get_contents($filename);
                $this->json = json_decode($fileContent);
            }
            else
                $this->json = NULL;
        }

        return $this->json;
    }

    private function checkPassword() {
        $receivedPass = $this->password;
        $storedPass = $this->getStoredPassword();
        if ($storedPass != $receivedPass)
            throw new ForbiddenRequestException("Password does not match saved password.");

        return $receivedPass;
    }

    private function getStoredPassword() {
        $this->loadLocalJSON();
        if ($this->json == NULL)
            throw new BadRequestException("Channel with id '".  $this->id."' does not exist.");

        $field = Tags::$TAG_CHANNEL_PASS;
        return $this->json->$field;
    }

    private function getRouteToJSON($id) {
        return DATA_DIR . '/' . $id . ".json";
    }

    private function createBaseJson() {
        $valueNickPair = new JSONObject();
        self::setVar($valueNickPair, Tags::$TAG_NICK);
        self::setVar($valueNickPair, Tags::$TAG_VALUE);

        $newJson = new JSONObject();
        self::setVar($newJson, Tags::$TAG_CHANNEL_NAME, $this->id);
        self::setVar($newJson, Tags::$TAG_CHANNEL_PASS, $this->password);
        self::setVar($newJson, Tags::$TAG_CHANGED, time());
        self::setVar($newJson, Tags::$TAG_TIMER, $valueNickPair);
        self::setVar($newJson, Tags::$TAG_DELETE, $valueNickPair);
        self::setVar($newJson, Tags::$TAG_USERS, array());

        return $newJson;
    }

    public static function setVar($stdClass, $variableName, $value = "") {
        $stdClass->$variableName = $value;
    }

    private static function getVar($stdClass, $variableName) {
        return $stdClass->$variableName;
    }

    private function addDataToJson($json, $userJson) {
        $usersTag = Tags::$TAG_USERS;
        $allUsers = &$json->$usersTag;
        $newNick = self::getVar($userJson, Tags::$TAG_NICK);
        $key = $this->getUserKeyInArray($allUsers, $newNick);

        if ($key === -1)    // not found
            $allUsers[] = $userJson;
        else
            $allUsers[$key] = $userJson;
    }

    private function getUserKeyInArray($users, $newNick) {
        foreach ($users as $key => $user)
            if (self::getVar($user, Tags::$TAG_NICK) == $newNick)
                return $key;

        return -1;
    }

    private static function createUserData($nick, $state, $reason) {
        $data = new JSONObject();
        self::setVar($data, Tags::$TAG_NICK, $nick);
        self::setVar($data, Tags::$TAG_STATE, $state);
        self::setVar($data, Tags::$TAG_REASON, $reason);
        return $data;
    }

    private function saveJson() {
        self::setVar($this->json, Tags::$TAG_CHANGED, time());
        $file = fopen($this->getRouteToJSON($this->id), "w");
        fwrite($file, json_encode($this->json));
        fclose($file);
    }
}