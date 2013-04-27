<?php

class Model extends Nette\Object
{
    /** @var Nette\Database\Connection */
    public $database;

    public function __construct(Nette\Database\Connection $database)
    {
        $this->database = $database;
    }

    /*
     * Access to tables
     */

    public function getUsers() {
        /*
         *  id, account, authToken, displayName
         */

        return $this->database->table("users");
    }

    public function getTeam() {
        /*
         *  id, name, passwordHash, changed, timerValue, timerAuthor, deleteReason, deleteAuthor
         */

        return $this->database->table("teams");
    }

    public function getTeamUsers() {
        /*
         *  userId, teamId, state, reason
         */
        return $this->database->table("user_teams");
    }

    /*
     * Helpers
     */

    /**
     * Returns user DB entry
     * @param String
     * @return ActiveRow or FALSE if there is no row
     */
    public function getUserByAccount($account, $authToken) {
        return $this->getUsers()
                ->where("account", $account)
                ->select("id, account, displayName, authToken = ".$authToken. " AS authOK")
                ->limit(1)
                ->fetch();
    }

    public function insertUser(array $data) {
        return $this->getUsers()
                ->insert($data);
    }

    public function getTeamByCredentials($teamName, $teamPass, $changed) {
        $where = array("name" => $teamName);
        if ($teamPass !== NULL)
            $where["password"] = $teamPass;

        $select = "id, name, timerVal, timerAuthor, deleteVal, deleteAuthor";
        if ($changed !== NULL)
            $select .= ", changed > ". $changed . " AS isModified";

        return $this->getTeam()
                ->where($where)
                ->select($select)
                ->fetch();
    }

    public function isUserInTeam($userId, $teamId) {
        return (boolean)$this->getTeamUsers()
                ->where(array("userId" => $userId, "teamId" => $teamId))
                ->limit(1)
                ->select("COUNT(1) > 0 AS isMember")
                ->fetch()
                ->isMember;
    }

    public function getUsersForTeam($teamId) {
        return $this->getTeamUsers()
                ->where("teamId", $teamId)
                ->select("userId, state, reason");
    }

    public function getDisplayNamesForIds(array $userIds) {
        return $this->getUsers()
                ->where("id", $userIds)
                ->select("id, displayName");
    }

    public function insertTeam(array $data) {
        return $this->getTeam()
                ->insert($data);
    }
 }
