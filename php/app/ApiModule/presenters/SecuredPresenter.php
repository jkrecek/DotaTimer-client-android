<?php

namespace ApiModule;

abstract class SecuredPresenter extends ApiPresenter {

    /** @var User*/
    protected $user;

    protected function handleShared() {
        $account = $this->request->get(Tags::TAG_ACCOUNT);
        $appToken = $this->request->get(Tags::TAG_APP_TOKEN);

        $userRecord = $this->model->getUsers()
                ->where(array("account" => $account, "appToken" => $appToken))
                ->limit(1)->fetch();

        if (!$userRecord)
            throw new UnauthorizedException("Login unsuccessfull - Account or token is invalid");

        $this->user = User::fromDb($userRecord);
    }

}