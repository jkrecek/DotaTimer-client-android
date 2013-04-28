<?php

namespace ApiModule;

use \Nette\Database\Table\ActiveRow;

class ValueAuthorPair {

    /** @var mixed int string */
    public $value;

    /** @var int */
    public $author;

    private function __construct() {

    }

    public static function create($value, $author) {
        $pair = new ValueAuthorPair();
        $pair->value = $value;
        $pair->author = $author;
        return $pair;
    }
}

class TeamUser {

    /** @var mixed int string */
    public $user;

    /** @var int */
    public $state;

    /** @var string */
    public $reason;

    private function __construct() {

    }

    /**
     *
     * @param int $userId
     * @param int $state
     * @param string $reason
     * @return \ApiModule\TeamUser
     */
    public static function create($userId, $state, $reason) {
        $user = new TeamUser();
        $user->user = $userId;
        $user->state = $state;
        $user->reason = $reason;
        return $user;
    }
}

class Team extends \stdClass {

    /** @var int */
    public $id;

    /** @var string */
    public $name;

    /** @var ValueAuthorPair */
    public $timer;

    /** @var ValueAuthorPair */
    public $delete;

    /** @var array TeamUsers */
    public $users = array();

    private function __construct() {

    }

    /**
     *
     * @param type $data
     * @param \Model $model
     * @return \ApiModule\Team
     */
    public static function createNewTeam($name, $passwordHash, \Model $model) {

        $data = array(
            "name" => $name,
            "password" => $passwordHash
        );

        $teamRow = $model->insertTeam($data);

        $team = self::fromDb($teamRow, $model);

        //$team = new Team();
        //$team->id = $model->database->lastInsertId();
        /*$user->account = $account;
        $user->displayName = $displayName;*/

        return $team;
    }

    public static function fromDb(ActiveRow $row, \Model $model) {
        $team = new Team();
        $team->id = $row->id;
        $team->name = $row->name;
        $team->changed = $row->changed;

        if (!empty($row->timerVal) && !empty($row->timerAuthor))
            $team->timer = ValueAuthorPair::create($row->timerVal, $row->timerAuthor);
        else
            $team->timer = ValueAuthorPair::create(NULL, NULL);

        if (!empty($row->deleteVal) && !empty($row->deleteAuthor))
            $team->delete = ValueAuthorPair::create($row->deleteVal, $row->deleteAuthor);
        else
            $team->delete = ValueAuthorPair::create(NULL, NULL);

        $team->loadUsers($model);

        return $team;
    }

    /**
     *
     * @param \ApiModule\User $user
     * @param boolean $withUser
     * @param string $teamName
     * @param \Model $model
     * @param int $changed must be NULL if forced
     * @return Team
     * @throws NotModifiedException
     * @throws BadRequestException
     */
    public static function fromDbUsersTeam(User $user, $withUsers, $teamName, \Model $model, $changed) {
        $dbTeamResult = $model->getTeamByCredentials($teamName, NULL, $changed);

        if (!empty($dbTeamResult)) {
            if ($changed && !$dbTeamResult->isModified)
                throw new NotModifiedException("Team was not modified");

            if ($model->isUserInTeam($user->id, $dbTeamResult->id))
                return self::fromDb($dbTeamResult, $withUsers ? $model : NULL);
        }

        throw new BadRequestException("User is not member of such team");
    }

    private function loadUsers(\Model $model) {
        $dbTeamUsers = $model->getUsersForTeam($this->id);
        foreach($dbTeamUsers as $teamUser)
            $this->users[] = TeamUser::create($teamUser->userId, $teamUser->state, $teamUser->reason);
    }

    public function toOutputForm(\Model $model) {
        if (empty($this->users))
            $this->loadUsers($model);

        $this->replaceUserIdsWithNames($model);
    }

    private function replaceUserIdsWithNames(\Model $model) {
        $collectedIds = array();
        if ($this->timer->author)
            $collectedIds[] = $this->timer->author;

        if ($this->delete->author)
            $collectedIds[] = $this->delete->author;

        foreach ($this->users as $user)
            $collectedIds[] = $user->user;

        $uniqueIds = array_unique($collectedIds);
        // values already converted on in bad format
        if (!Method::containtsOnlyInt($uniqueIds))
            return;

        $searchCount = count($uniqueIds);
        $dbDisplayNames = $model->getDisplayNamesForIds($uniqueIds);
        $displayNames = array();
        foreach ($dbDisplayNames as $dbLine)
             $displayNames[$dbLine->id] = $dbLine->displayName;

        $resultCount = count($displayNames);
        if ($searchCount != $resultCount)
            throw new BadRequestException("Some user references are missing (".$searchCount-$resultCount.")");

        if ($this->timer->author)
            $this->timer->author = $displayNames[$this->timer->author];
        if ($this->delete->author)
            $this->delete->author = $displayNames[$this->delete->author];

        foreach ($this->users as $user)
            $user->user = $displayNames[$user->user];
    }
}

