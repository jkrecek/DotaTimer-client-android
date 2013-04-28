<?php
namespace ApiModule;

class DataPresenter extends ApiPresenter {

    /** @var User */
    /*private $user;

    //private $password;

    final protected function handleShared() {
        $this->checkRequest();
    }

    final protected function handleRead() {
        $updateSince = $this->request->get(Tags::TAG_CHANGED);

        $team = Team::fromDbUsersTeam($this->user, true, $this->id, $this->model, $updateSince);
        $team->replaceUserIdsWithNames($this->model);

        $this->response = ApiJSON::load(json_encode($team));
        $this->responser->OK();
    }

    final protected function handleCreate() {
        $this->loadLocalJSON();
        if ($this->json == NULL)
            $this->json = $this->createBaseJson();
        else
            $this->checkPassword();

        $this->addDataToJson($this->json, self::createUserData($this->nick, "", ""));
        $this->saveJson();

        $this->responser->Created($this->json);
    }

    final protected function handleUpdate() {
        $this->loadLocalJSON();
        $this->checkPassword();

        if (self::getVar($this->request, Tags::TAG_TIMER)) {
            $timerTarget = self::getVar($this->request, Tags::TAG_TIMER);

            // timer
            $newTimerObj = new JSONObject();
            self::setVar($newTimerObj, Tags::TAG_VALUE, $timerTarget);
            self::setVar($newTimerObj, Tags::TAG_NICK, $this->nick);
            self::setVar($this->json, Tags::TAG_TIMER, $newTimerObj);

            // delete
            $newDeleteObj = new JSONObject();
            self::setVar($newDeleteObj, Tags::TAG_VALUE);
            self::setVar($newDeleteObj, Tags::TAG_NICK);
            self::setVar($this->json, Tags::TAG_DELETE, $newDeleteObj);

            // users
            $allUsers = self::getVar($this->json, Tags::TAG_USERS);
            foreach ($allUsers as $user) {
                self::setVar($user, Tags::TAG_STATE);
                self::setVar($user, Tags::TAG_REASON);
            }
        } elseif(self::getVar($this->request, Tags::TAG_DELETE)) {
            $deleteReason = self::getVar($this->request, Tags::TAG_DELETE);

            $newObj = new JSONObject();
            self::setVar($newObj, Tags::TAG_VALUE, $deleteReason);
            self::setVar($newObj, Tags::TAG_NICK, $this->nick);

            self::setVar($this->json, Tags::TAG_DELETE, $newObj);
        } elseif (self::getVar($this->request, Tags::TAG_STATE)) {
            $state = self::getVar($this->request, Tags::TAG_STATE);
            $reason = self::getVar($this->request, Tags::TAG_REASON);
            if (!$reason)
                $reason = "";

            $this->addDataToJson($this->json, self::createUserData($this->nick, $state, $reason));
        } else
            throw new BadRequestException("Nothing to do");

        $this->saveJson();

        $this->responser->Accepted($this->json);
    }


    private function checkRequest() {
        if (!$this->id)
            $this->id = $this->request->get(Tags::TAG_TEAM_NAME);

        if (!$this->id)
            throw new BadRequestException("Name of team must be supplied.");

        $account = $this->request->get(Tags::TAG_ACCOUNT);
        $authToken = $this->request->get(Tags::TAG_AUTH_TOKEN);

        $dbUserData = $this->model->getUserByAccount($account, $authToken);

        if (!$dbUserData->authOK)
            throw new UnauthorizedException("Incorrect auth token.");

        $this->user = User::fromDb($dbUserData);
    }

    *//*private function loadLocalJSON() {
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
    }*//*

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

        $field = Tags::TAG_CHANNEL_PASS;
        return $this->json->$field;
    }

    private function getRouteToJSON($id) {
        return DATA_DIR . '/' . $id . ".json";
    }

    private function createBaseJson() {
        $valueNickPair = new JSONObject();
        self::setVar($valueNickPair, Tags::TAG_VALUE);
        self::setVar($valueNickPair, Tags::TAG_NICK);

        $newJson = new JSONObject();
        self::setVar($newJson, Tags::TAG_CHANNEL_NAME, $this->id);
        self::setVar($newJson, Tags::TAG_CHANNEL_PASS, $this->password);
        self::setVar($newJson, Tags::TAG_CHANGED, time());
        self::setVar($newJson, Tags::TAG_TIMER, $valueNickPair);
        self::setVar($newJson, Tags::TAG_DELETE, $valueNickPair);
        self::setVar($newJson, Tags::TAG_USERS, array());

        return $newJson;
    }

    private function addDataToJson($json, $userJson) {
        $usersTag = Tags::TAG_USERS;
        $allUsers = &$json->$usersTag;
        $newNick = self::getVar($userJson, Tags::TAG_NICK);
        $key = $this->getUserKeyInArray($allUsers, $newNick);

        if ($key === -1)    // not found
            $allUsers[] = $userJson;
        else
            $allUsers[$key] = $userJson;
    }

    private function getUserKeyInArray($users, $newNick) {
        foreach ($users as $key => $user)
            if (self::getVar($user, Tags::TAG_NICK) == $newNick)
                return $key;

        return -1;
    }

    private static function createUserData($nick, $state, $reason) {
        $data = new JSONObject();
        self::setVar($data, Tags::TAG_NICK, $nick);
        self::setVar($data, Tags::TAG_STATE, $state);
        self::setVar($data, Tags::TAG_REASON, $reason);
        return $data;
    }

    private function saveJson() {
        self::setVar($this->json, Tags::TAG_CHANGED, time());
        $file = fopen($this->getRouteToJSON($this->id), "w");
        fwrite($file, json_encode($this->json));
        fclose($file);
    }*/
}
