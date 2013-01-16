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

    private $request;

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
            $updateSince = self::getVar($this->request, Tags::$TAG_CHANGED);
            if (!$updateSince || $updateSince == self::getVar($this->json, Tags::$TAG_CHANGED))
                ApiResponser::returnOK();
            else
                ApiResponser::returnUnchanged();
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
        try {
            $this->setInitial($id, $data);

            $this->loadLocalJSON();
            $this->checkPassword();

            if (self::getVar($this->request, Tags::$TAG_TIMER)) {
                $timerTarget = self::getVar($this->request, Tags::$TAG_TIMER);

                // timer
                $newTimerObj = new JSONObject();
                self::setVar($newTimerObj, Tags::$TAG_VALUE, $timerTarget);
                self::setVar($newTimerObj, Tags::$TAG_NICK, $this->nick);
                self::setVar($this->json, Tags::$TAG_DELETE, $newTimerObj);

                // delete
                $newDeleteObj = new JSONObject();
                self::setVar($newDeleteObj, Tags::$TAG_VALUE);
                self::setVar($newDeleteObj, Tags::$TAG_NICK);
                self::setVar($this->json, Tags::$TAG_TIMER, $newDeleteObj);

                // users
                $allUsers = self::getVar($this->json, Tags::$TAG_USERS);
                foreach ($allUsers as $user) {
                    self::setVar($user, Tags::$TAG_STATE);
                    self::setVar($user, Tags::$TAG_REASON);
                }
            } elseif(self::getVar($this->request, Tags::$TAG_DELETE)) {
                $deleteReason = self::getVar($this->request, Tags::$TAG_DELETE);

                $newObj = new JSONObject();
                self::setVar($newObj, Tags::$TAG_VALUE, $deleteReason);
                self::setVar($newObj, Tags::$TAG_NICK, $this->nick);

                self::setVar($this->json, Tags::$TAG_DELETE, $newObj);
            } elseif (self::getVar($this->request, Tags::$TAG_STATE)) {
                $state = self::getVar($this->request, Tags::$TAG_STATE);
                $reason = self::getVar($this->request, Tags::$TAG_REASON);
                if (!$reason)
                    $reason = "";

                $this->addDataToJson($this->json, self::createUserData($this->nick, $state, $reason));
            } else
                throw new BadRequestException("Nothing to do");

            $this->saveJson();
            ApiResponser::returnAccepted();
        } catch (ForbiddenRequestException $e) {
            ApiResponser::returnForbidden($e->getMessage());
        } catch (BadRequestException $e) {
            ApiResponser::returnBadRequest($e->getMessage());
        }
    }


    private function setInitial($id, $request) {
        $jsonString = is_array($request) ? json_encode($request) : $request;
        $this->request = json_decode($jsonString);

        if (!$this->request)
            throw new BadRequestException("Bad values supplied.");

        $this->id = $id ? $id : self::getVar($this->request, Tags::$TAG_CHANNEL_NAME);
        if (!$this->id)
            throw new ForbiddenRequestException("Channel name is required.");

        $this->password = self::getVar($this->request, Tags::$TAG_CHANNEL_PASS);
        if (!$this->password)
            throw new ForbiddenRequestException("JSON: Password is required.");

        $this->nick = self::getVar($this->request, Tags::$TAG_NICK);
        if (!$this->nick)
            throw new BadRequestException("JSON: Author nick is required.");
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
        self::setVar($valueNickPair, Tags::$TAG_VALUE);
        self::setVar($valueNickPair, Tags::$TAG_NICK);

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
        if (isset($stdClass->$variableName))
            return $stdClass->$variableName;
        else
            return NULL;
    }

    private function addDataToJson($json, $userJson) {
        /*$usersTag = Tags::$TAG_USERS;
        $allUsers = &$json->$usersTag;*/
        $allUsers = self::getVar($json, Tags::$TAG_USERS);
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