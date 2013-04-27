<?php

namespace ApiModule;

use \Nette\Database\Table\ActiveRow;

class User {

    /** @var int */
    public $id;

    /** @var string */
    public $account;

    /** @var string */
    public $displayName;

    private function __construct() {

    }


    /**
     *
     * @param string $account
     * @param string $displayName
     * @param \Model $model
     * @return string appToken
     * @throws ForbiddenException
     * @throws \ApiModule\PDOException
     */

    public static function createNewUser($account, $googleToken, $displayName, \Model $model) {
        $data = array(
            "account" => $account,
            "googleToken" => $googleToken,
            "appToken" => Method::generateString(40),
            "displayName" => $displayName
        );

        try {
            $row = $model->insertUser($data);
        } catch (\PDOException $e) {
            if ($e->errorInfo[0] == 23000 && $e->errorInfo[1] = 1062) {
                $duplicateColumn = Method::lastWord($e->errorInfo[2], true);
                if ($duplicateColumn == Tags::TAG_ACCOUNT)
                    throw new ForbiddenException("User with this account already exists");
                else if ($duplicateColumn == Tags::TAG_DISPLAY_NAME)
                    throw new ForbiddenException("User with this name already exists");
            }

            throw $e;
        }

        //$user = User::fromDb($row, true);

        return $row->appToken;
    }

    public static function fromDb(ActiveRow $row/*, $loadAuth = false*/) {
        $user = new User();
        $user->id = $row->id;
        $user->account = $row->account;
        $user->displayName = $row->displayName;

        /*if ($loadAuth)
            $user->appToken = $row->appToken;*/

        return $user;
    }

}


