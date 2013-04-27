<?php
namespace ApiModule;

class ApiPresenter extends \BasePresenter {

    /** @var ApiResponser */
    protected $responser;

    /** @var \Model */
    protected $model;

    /** @var ApiJSON */
    protected $request;

    /** @var ApiJSON */
    protected $response;

    /** @var int */
    protected $id;

    protected function startup() {
        parent::startup();

        $this->responser = new ApiResponser($this);
        $this->model = $this->getService('model');
    }

    final public function getResponse() {
        return $this->getHttpResponse();
    }

    final public function actionRead($format, $data, $id, $query = array(), $associations = array()) {
        try {
            $this->loadRequest($format, $id, $query);
            $this->handleShared();
            $this->handleRead();
        } catch(\Exception $e) {
            $this->responser->exceptionEscape($e);
        }
    }

    final public function actionCreate($format, $data, $id, $query = array(), $associations = array()) {
        try {
            $this->loadRequest($format, $id, $data);
            $this->handleShared();
            $this->handleCreate();
        } catch(\Exception $e) {
            $this->responser->exceptionEscape($e);
        }
    }

    final public function actionUpdate($format, $data, $id, $query = array(), $associations = array()) {
        try {
            $this->loadRequest($format, $id, $data);
            $this->handleShared();
            $this->handleUpdate();
        } catch(\Exception $e) {
            $this->responser->exceptionEscape($e);
        }
    }

    private function loadRequest($format, $id, $request) {
        if ($format != "json")
            throw new NoContentException("Api supports only json requests");

        $this->id = $id;

        $jsonString = is_array($request) ? json_encode($request) : $request;
        $this->request = ApiJSON::load($jsonString);
    }

    protected function handleRead() {
        throw new NoContentException("Api does not support this request");
    }
    protected function handleCreate() {
        throw new NoContentException("Api does not support this request");
    }
    protected function handleUpdate() {
        throw new NoContentException("Api does not support this request");
    }

    /**
     * Method called for all requests, optional
     */

    protected function handleShared() {

    }

}