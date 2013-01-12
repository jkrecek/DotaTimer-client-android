<?php
namespace ApiModule;

use \Tags as Tags;

class DataPresenter extends \BasePresenter {


    private $json;

    private $id;
    private $nick;
    private $password;

    public function actionRead($format, $data, $id, $query = array(), $associations = array()) {
        $this->setInitial($id, $query);
        $this->createBaseJson();
        try {
            $this->checkPassword($id, $query);
        } catch (\Nette\Application\ForbiddenRequestException $e) {
            echo "Forbidden - ".$e->getMessage() ;
        } catch (\Nette\Application\BadRequestException $e) {
            echo "BadRequest - ".$e->getMessage();
        }
    }

    public function renderRead() {

    }

    public function actionCreate($format, $data, $id, $query = array(), $associations = array()) {
        $this->setInitial($id, $data);
        $localData = $this->getLocalJSON($this->id);
        if ($localData == NULL)
            $this->json = $this->createBaseJson($this->id, $this->password);
        else {
            try {
                $this->checkPassword();
            } catch (\Nette\Application\ForbiddenRequestException $e) {
                $this->redirect(400, "Error:400");
            }
        }

        $this->addDataToJson($this->json, self::createUserData($this->nick, "", ""));
        $this->saveJson();
        $this->redirect(201, "Error:201");
    }

    public function renderCreate() {

    }

    public function actionUpdate($format, $data, $id, $query = array(), $associations = array()) {
        $this->setInitial($id, $data);
    }

    public function renderUpdate() {
        $this->saveJson();
        $this->redirect(202, "Error:202");
    }


    private function setInitial($id, $request) {
        $this->id = $id;
        try {
            $this->password = $this->setFromRequest($request);
        } catch (\Nette\Application\ForbiddenRequestException $e) {
            $this->redirect(400, "Error:400");
        }
    }
    private function checkPassword() {
        $receivedPass = $this->password;
        $storedPass = $this->getStoredPassword();
        if ($storedPass != $receivedPass)
            throw new \Nette\Application\ForbiddenRequestException("Password does not match saved password.");

        return $receivedPass;
    }

    private function setFromRequest($request) {
        if (is_array($request)) {
            if (!array_key_exists(Tags::$TAG_CHANNEL_PASS, $request))
                throw new \Nette\Application\ForbiddenRequestException("Password is required.");
            $this->password = $request[Tags::$TAG_CHANNEL_PASS];

            if (!array_key_exists(Tags::$TAG_NICK, $request))
                throw new \Nette\Application\ForbiddenRequestException("Author nick is required.");
            $this->nick = $request[Tags::$TAG_NICK];

        } elseif(is_object($request)) {
            $data = json_decode($request);
            $fieldPass = Tags::$TAG_CHANNEL_PASS;
            if (!isset($data->$fieldPass))
                throw new \Nette\Application\ForbiddenRequestException("Password is required.");
            $this->password = $request->$fieldPass;

            $fieldNick = Tags::$TAG_NICK;
            if (!isset($data->$fieldNick))
                throw new \Nette\Application\ForbiddenRequestException("Author nick is required.");
            $this->nick = $request->$fieldNick;
        }
    }

    private function getLocalJSON($id) {
        if ($this->json == NULL) {
            $filename = DATA_DIR . $this->idToFileName($id);
            if (file_exists($filename)) {
                $fileContent = file_get_contents($filename);
                $this->json = json_decode($fileContent);
            }
        }

        return $this->json;
    }

    private function getStoredPassword() {
        $json = $this->getLocalJSON($this->id);
        if (json == NULL)
            throw new \Nette\Application\BadRequestException("Channel with id '".  $this->id."' does not exist.");

        $field = Tags::$TAG_CHANNEL_PASS;
        return $json->$field;
    }

    private function idToFileName($id) {
        return $id . ".json";
    }

    private function createBaseJson($channelName, $channelPass) {
        $valueNickPair = new \stdClass();
        $this->setVar($valueNickPair, Tags::$TAG_NICK);
        $this->setVar($valueNickPair, Tags::$TAG_VALUE);

        $newJson = new \stdClass();
        $this->setVar($newJson, Tags::$TAG_CHANNEL_NAME, $channelName);
        $this->setVar($newJson, Tags::$TAG_CHANNEL_PASS, $channelPass);
        $this->setVar($newJson, Tags::$TAG_TIMER, $valueNickPair);
        $this->setVar($newJson, Tags::$TAG_DELETE, $valueNickPair);
        $this->setVar($newJson, Tags::$TAG_USERS, array());

        return $newJson;
    }

    private function setVar($stdClass, $variableName, $value = "") {
        $stdClass->$variableName = $value;
        $changed = Tags::$TAG_CHANGED;
        $stdClass->$changed = time();
    }

    private function getVar($stdClass, $variableName) {
        return $stdClass->$variableName;
    }

    private function addDataToJson($json, $userJson)
    {
        $usersTag = Tags::$TAG_USERS;
        $allUsers = &$json->$usersTag;
        $newNick = $this->getVar($userJson, Tags::$TAG_NICK);
        $key = $this->getUserKeyInArray($allUsers, $newNick);

        if ($key === -1)    // not found
            $key = "";

        $allUsers[$key] = $userJson;
    }

    private function getUserKeyInArray($users, $newNick) {
        foreach ($users as $key => $user)
            if ($this->getVar($user, Tags::$TAG_NICK) == $newNick)
                return $key;

        return -1;
    }

    private static function createUserData($nick, $state, $reason) {
        $data = new \stdClass();
        $this->setVar($data, Tags::$TAG_NICK, $nick);
        $this->setVar($data, Tags::$TAG_STATE, $state);
        $this->setVar($data, Tags::$TAG_REASON, $reason);
        return $data;
    }

    private function saveJson() {
        $file = fopen($this->idToFileName($this->id), "w");
        fwrite($file, json_encode($this->json));
        fclose($file);
    }
}