<?php

namespace ApiModule;

use \Nette\Application\BadRequestException as NetteException,
    Nette\Http\Response;

class BaseException extends NetteException {
    /** @var string */
    protected $name;

    public function getStatusCode() {
        return $this->defaultCode;
    }

    public function getName() {
        return $this->name;
    }
}

class BadRequestException extends BaseException {
    /**
     * may not be supplied, already set in Nette Bad Request Exception
     * @var int
     */

    /** @var int */
    protected $defaultCode = Response::S400_BAD_REQUEST;

    /** @var string */
    protected $name = "Bad request";
}

class ForbiddenException extends BaseException {
    /** @var int */
    protected $defaultCode = Response::S403_FORBIDDEN;

    /** @var string */
    protected $name = "Forbidden";
}

class UnauthorizedException extends BaseException {
    /** @var int */
    protected $defaultCode = Response::S401_UNAUTHORIZED;

    /** @var string */
    protected $name = "Unauthorized";

}

class NoContentException extends BaseException {
    /** @var int */
    protected $defaultCode = Response::S204_NO_CONTENT;

    /** @var string */
    //protected $name = "No content";

}

class NotModifiedException extends BaseException {
    /** @var int */
    protected $defaultCode = Response::S304_NOT_MODIFIED;

    /** @var string */
    protected $name = "Not modified";
}