<?php

namespace ApiModule;

class TeamPresenter extends SecuredPresenter {

    protected function handleShared() {
        parent::handleShared();

        if (!$this->id) {
            $this->id = $this->request->get(Tags::TAG_TEAM_NAME);
            if (!$this->id)
                throw new BadRequestException("Unknown team id");
        }
    }

    protected function handleCreate() {
        $teamPassword = $this->request->get(Tags::TAG_TEAM_PASS);

        $teamRow = $this->model->getTeams()->where("name", $this->id)->limit(1)->fetch();

        $team = empty($teamRow) ? Team::createNewTeam($this->id, $teamPassword, $this->model) : Team::fromDb($teamRow, $this->model);
        if ($this->model->isUserInTeam($this->user->id, $team->id))
            throw new BadRequestException("User is already part of the team");

        $this->model->getTeamUsers()->insert(array(
            "userId" => $this->user->id ,
            "teamId" => $team->id
        ));

        $this->model->flushTeamChange($team->id);

        $team->toOutputForm($this->model);
        $this->responser->Created($team);
    }

    protected function handleRead() {
        $changed = $this->request->get(Tags::TAG_CHANGED);

        $teamRow = $this->model->getTeamByCredentials($this->id, NULL, $changed);
        if (empty($teamRow))
            throw new BadRequestException("No such team exists");

        if (!$this->model->isUserInTeam($this->user->id, $teamRow->id))
            throw new ForbiddenException("User is not a member of the team");

        if (!$teamRow->isModified)
            throw new NotModifiedException();

        $team = Team::fromDb($teamRow, $this->model);
        $team->toOutputForm($this->model);
        $this->responser->OK($team);
    }

    protected function handleUpdate() {
        $teamRow = $this->model->getTeamByCredentials($this->id, NULL, NULL);
        if (empty($teamRow))
            throw new BadRequestException("No such team exists");

        if (!$this->model->isUserInTeam($this->user->id, $teamRow->id))
            throw new ForbiddenException("User is not a member of the team");

        if ($this->request->has(Tags::TAG_TIMER)) {
            $timer = $this->request->has(Tags::TAG_TIMER);

            $this->model->getTeams()->where("id", $teamRow->id)->update(array(
                "timerVal" => $timer,
                "timerAuthor" => $this->user->id,
                "deleteVal" => NULL,
                "deleteAuthor" => NULL
            ));

            $this->model->getTeamUsers()->where("teamId", $teamRow->id)->update(array(
                "state" => 0,
                "reason" => ""
            ));
        }
        elseif ($this->request->has(Tags::TAG_DELETE)) {
            $delete = $this->request->get(Tags::TAG_DELETE);

            $this->model->getTeams()->where("id", $teamRow->id)->update(array(
                "deleteVal" => $delete,
                "deleteAuthor" => $this->user->id
            ));
        }
        elseif ($this->request->has(Tags::TAG_STATE)) {
            $state = $this->request->get(Tags::TAG_STATE);
            $reason = $this->request->has(Tags::TAG_REASON) ? $this->request->get(Tags::TAG_REASON) : "";

            $this->model->getTeamUsers()->where(array(
                "userId" => $this->user->id,
                "teamId" => $teamRow->id
            ))->update(array(
                "state" => $state,
                "reason" => $reason
            ));
        }

        $team = Team::fromDb($this->model->getTeams()->find($team->id), $this->model);
        $team->toOutputForm($this->model);
        $this->responser->Accepted($team);
    }
}