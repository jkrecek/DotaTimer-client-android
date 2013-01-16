<?php

namespace ApiModule;

use Nette\Application\Responses\TextResponse,
    Nette\Application\Responses\JsonResponse;

class ApiResponser {

    public static function returnBadRequest($additional="") {
        self::getResponse(DataPresenter::$instance)->setCode(\Nette\Http\Response::S400_BAD_REQUEST);
        $response = "Bad request";
        if ($additional)
            $response .= " - '".$additional."'";

        DataPresenter::$instance->sendResponse(new TextResponse($response));
    }

    public static function returnUnauthorized($additional="") {
        self::getResponse(DataPresenter::$instance)->setCode(\Nette\Http\Response::S401_UNAUTHORIZED);
        $response = "Unauthorized";
        if ($additional)
            $response .= " - '".$additional."'";

        DataPresenter::$instance->sendResponse(new TextResponse($response));
    }

    public static function returnForbidden($additional="") {
        self::getResponse(DataPresenter::$instance)->setCode(\Nette\Http\Response::S403_FORBIDDEN);
        $response = "Forbidden";
        if ($additional)
            $response .= " - '".$additional."'";

        DataPresenter::$instance->sendResponse(new TextResponse($response));
    }

    public static function returnOK() {
        self::getResponse(DataPresenter::$instance)->setCode(\Nette\Http\Response::S200_OK);
        DataPresenter::$instance->sendResponse(new JsonResponse(self::passwordlessJSON(DataPresenter::$instance->json)));
    }

    public static function returnCreated() {
        self::getResponse(DataPresenter::$instance)->setCode(201);
        DataPresenter::$instance->sendResponse(new JsonResponse(self::passwordlessJSON(DataPresenter::$instance->json)));
    }

    public static function returnAccepted() {
        self::getResponse(DataPresenter::$instance)->setCode(202);
        DataPresenter::$instance->sendResponse(new JsonResponse(self::passwordlessJSON(DataPresenter::$instance->json)));
    }

    public static function returnUnchanged() {
        self::getResponse(DataPresenter::$instance)->setCode(\Nette\Http\Response::S200_OK);
        DataPresenter::$instance->sendResponse(new TextResponse("Unchanged"));
    }

    private static function getResponse() {
        return DataPresenter::$instance->getService('httpResponse');
    }

    private static function passwordlessJSON($json) {
        DataPresenter::setVar($json, Tags::$TAG_CHANNEL_PASS);
        return $json;
    }
}


