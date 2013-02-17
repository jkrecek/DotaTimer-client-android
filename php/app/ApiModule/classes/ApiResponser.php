<?php

namespace ApiModule;

use \Nette\Application\Responses\TextResponse,
    \Nette\Application\Responses\JsonResponse,
    \Nette\Http\Response;



class ApiResponser {

    /** @var ApiPresenter */
    private $presenter;

    /** @var Nette\Http\Response */
    private $response;

    public function __construct(ApiPresenter $presenter) {
        $this->presenter = $presenter;
        //$this->response = $this->presenter->getService('httpResponse');
        $this->response = $this->presenter->getResponse();
    }

    /*
     * Exception handle
     */

    public function exceptionEscape(\Exception $e) {
        $additionalMessage = $e->getMessage();

        if ($e instanceof BaseException) {
            $this->response->setCode($e->getStatusCode());
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

    public function OK($json) {
        $this->response->setCode(Response::S200_OK);
        $this->presenter->sendResponse(new JsonResponse($json));
    }

    public function Created($json) {
        $this->response->setCode(201);
        $this->presenter->sendResponse(new JsonResponse($json));
    }

    public function Accepted($json) {
        $this->response->setCode(202);
        $this->presenter->sendResponse(new JsonResponse($json));
    }

    /*public function Unchanged() {
        $this->response->setCode(Response::S304_NOT_MODIFIED);
        $this->presenter->sendResponse(new TextResponse("Not modified"));
    }*/

    /*private function passwordlessJSON($json) {
        DataPresenter::setVar($json, Tags::TAG_CHANNEL_PASS);
        return $json;
    }*/
}


