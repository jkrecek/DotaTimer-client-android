<?php

// absolute filesystem path to this web root
define('WWW_DIR', "");

// absolute filesystem path to the application root
define('APP_DIR', WWW_DIR . '../app');

// absolute filesystem path to the libraries
define('LIBS_DIR', WWW_DIR . '../libs');

// absolute path to private dir
define('PRIVATE_DIR', APP_DIR . '/private');

// absolute path to private cache files dir
define('DATA_DIR', PRIVATE_DIR . '/data');

// Uncomment this line if you must temporarily take down your site for maintenance.
// require '.maintenance.php';

// Let bootstrap create Dependency Injection container.
$container = require __DIR__ . '/../app/bootstrap.php';



// Run application.
$container->application->run();
