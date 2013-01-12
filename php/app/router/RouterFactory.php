<?php

use Nette\Application\Routers\RouteList,
    Nette\Application\Routers\Route,
    AdamStipak\RestRoute;


/**
 * Router factory.
 */
class RouterFactory
{

    /**
     * @return Nette\Application\IRouter
     */
    public function createRouter()
    {
        $router = new RouteList();
        $router[] = new Route('index.php', 'Homepage:default', Route::ONE_WAY);
        $router[] = new RestRoute('Api', array('json') );
        $router[] = new Route('<presenter>/<action>[/<id>]', 'Homepage:default');
        return $router;
    }

}
