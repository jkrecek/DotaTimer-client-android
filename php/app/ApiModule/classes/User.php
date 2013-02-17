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
     * @param String
     */

    public static function createNewUser($account, $displayName, \Model $model) {
        $user = new User();
        $data = array(
            "account" => $account,
            "authToken" => Method::generateString(40),
            "displayName" => $displayName
        );
        $model->insertUser($data);

        $user->id = $model->database->lastInsertId();
        $user->account = $account;
        $user->displayName = $displayName;

        return $user;
    }

    public static function fromDb(ActiveRow $row) {
        $user = new User();
        $user->id = $row->id;
        $user->account = $row->account;
        $user->displayName = $row->displayName;
        return $user;
    }

}


