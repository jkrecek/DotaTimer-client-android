<?php

namespace ApiModule;

class UserPresenter extends ApiPresenter {

    public function handleCreate() {
        $account = $this->request->get(Tags::TAG_ACCOUNT);
        $googleToken = $this->request->get(Tags::TAG_GOOGLE_TOKEN);
        $displayName = $this->request->get(Tags::TAG_DISPLAY_NAME);

        $appToken = User::createNewUser($account, $googleToken, $displayName, $this->model);

        $this->response = ApiJSON::getEmptyInstance();
        $this->response->set(Tags::TAG_APP_TOKEN, $appToken);
        $this->responser->Created();
    }

    public function handleRead() {
        $googleToken = $this->request->get(Tags::TAG_GOOGLE_TOKEN);

        $userRecord = $this->model->getUsers()->where("account", $this->id)->limit(1)->fetch();
        if (!$userRecord)
            throw new UnauthorizedException("User does not exist");

        if ($userRecord->googleToken != $googleToken)
            throw new UnauthorizedException("Google token does not match");

        $this->response = ApiJSON::getEmptyInstance();
        $this->response->set(Tags::TAG_APP_TOKEN, $userRecord->appToken);
        $this->responser->Created();
    }
}