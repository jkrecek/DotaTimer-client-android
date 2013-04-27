<?php

namespace ApiModule;

use \Nette\Application\Responses\TextResponse,
    \Nette\Application\Responses\JsonResponse,
    \Nette\Http\Response;



class ApiResponser {

    /** @var ApiPresenter */
    private $presenter;

    /** @var Nette\Http\Response */
    private $httpResponse;

    public function __construct(ApiPresenter $presenter) {
        $this->presenter = $presenter;
        //$this->httpResponse = $this->presenter->getService('httpResponse');
        $this->httpResponse = $this->presenter->getResponseHeader();
    }

    /*
     * Exception handle
     */

    public function exceptionEscape(\Exception $e) {
        $additionalMessage = $e->getMessage();

        if ($e instanceof BaseException) {
            $this->httpResponse->setCode($e->getStatusCode());
            $response = $e->getName();
            if ($additionalMessage)
                $response .= " - '" . $additionalMessage . "'";

            $this->presenter->sendResponse(new TextResponse($response));
        }
        else
            throw $e;
    }

    /*
     * Success handlers
     */

    public function OK($json = NULL) {
        $this->httpResponse->setCode(Response::S200_OK);
        $this->presenter->sendResponse($this->getReponse($json));
    }

    public function Created($json = NULL) {
        $this->httpResponse->setCode(201);
        $this->presenter->sendResponse($this->getReponse($json));
    }

    public function Accepted($json = NULL) {
        $this->httpResponse->setCode(202);
        $this->presenter->sendResponse($this->getReponse($json));
    }

    /**
     * Helper
     */

    private function getReponse($json = NULL) {
        if ($json != NULL)
            return new JsonResponse($json);
        else
            return new JsonResponse($this->presenter->getResponseJson());
    }

    /*public function Unchanged() {
        $this->httpResponse->setCode(Response::S304_NOT_MODIFIED);
        $this->presenter->sendResponse(new TextResponse("Not modified"));
    }*/

    /*private function passwordlessJSON($json) {
        DataPresenter::setVar($json, Tags::TAG_CHANNEL_PASS);
        return $json;
    }*/
}


