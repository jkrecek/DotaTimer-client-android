<?php
namespace ApiModule;

abstract class ApiPresenter extends \BasePresenter {

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

    final public function getResponseHeader() {
        return $this->getHttpResponse();
    }

    final public function getResponseJson() {
        return $this->response->toJson();
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
        if ($format != "json" && $this->getRequest()->getMethod() != "GET")
            throw new BadRequestException("Api supports only json requests");

        $this->id = $id;

        $jsonString = is_array($request) ? json_encode($request) : $request;
        $this->request = ApiJSON::load($jsonString);
    }

    protected function handleRead() {
        throw new BadRequestException("Api does not support read request");
    }
    protected function handleCreate() {
        throw new BadRequestException("Api does not support create request");
    }
    protected function handleUpdate() {
        throw new BadRequestException("Api does not support update request");
    }

    /**
     * Method called for all requests, optional
     */

    protected function handleShared() {

    }

}