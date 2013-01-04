<?php
static $jsonFilename = "dotatimer.json";

run();

function run()
{
    if (!isset($_POST['pass']) || !checkPass($_POST['pass']))
        die("Wrong password!");

    $params = loadParameters();
    $result = NULL;
    if (array_key_exists("delete_reason", $params))
        $result = handleDelete($params);
    elseif (array_key_exists("timer", $params))
        $result = handleChange($params);
    else
        die("Wrong action!");

    if ($result)
        echo $result;
}

function loadParameters()
{
    $parameters = array();
    foreach ($_GET as $key => $value)
        $parameters[$key] = $value;

    return $parameters;
}

function checkPass($pass)
{
    return $pass == "UcMsc3kYdXHi5KvhI6MRTfMxPOLfB8";
}

function handleChange($params)
{
    $target = new stdClass();
    $target->timer = $params["timer"];
    $target->set_by = $params["set_by"];
    $target->delete_reason = "";
    $target->delete_by = "";

    saveData($target);
}

function handleDelete($params)
{
    global $jsonFilename;
    $target = new stdClass();
    if (file_exists($jsonFilename))
    {
        $content = file_get_contents($jsonFilename);
        $target = json_decode($content);
    }

    $target->delete_by = $params["delete_by"];
    $target->delete_reason = $params["delete_reason"];

    saveData($target);
}

function saveData($target)
{
    global $jsonFilename;
    $targetJson = json_encode($target);

    $file = fopen($jsonFilename, 'w');
    fwrite($file, $targetJson);
    fclose($file);

    echo "OK";
}