﻿function downloadFile($url, $targetFile)
{
    "Downloading $url"
    $uri = New-Object "System.Uri" "$url"
    $request = [System.Net.HttpWebRequest]::Create($uri)
    $request.set_Timeout(15000) #15 second timeout
    $response = $request.GetResponse()
    $totalLength = [System.Math]::Floor($response.get_ContentLength()/1024)
    $responseStream = $response.GetResponseStream()
    $targetStream = New-Object -TypeName System.IO.FileStream -ArgumentList $targetFile, Create
    $buffer = new-object byte[] 10KB
    $count = $responseStream.Read($buffer,0,$buffer.length)
    $downloadedBytes = $count
    while ($count -gt 0)
    {
        [System.Console]::CursorLeft = 0
        [System.Console]::Write("Downloaded {0}K of {1}K", [System.Math]::Floor($downloadedBytes/1024), $totalLength)
        $targetStream.Write($buffer, 0, $count)
        $count = $responseStream.Read($buffer,0,$buffer.length)
        $downloadedBytes = $downloadedBytes + $count
    }
    "nFinished Download"
    $targetStream.Flush()
    $targetStream.Close()
    $targetStream.Dispose()
    $responseStream.Dispose()
}


$ikeaPath = $home+"\ikea";
$mavenPath = $ikeaPath + "\maven";
$url = "http://mirrors.besplatnyeprogrammy.ru/apache/maven/maven-3/3.1.1/binaries/apache-maven-3.1.1-bin.zip"
if(!(Test-Path $ikeaPath)) {
	New-Item -itemType directory -Path $ikeaPath;
	New-Item -itemType directory -Path $ikeaPath"/source";
	New-Item -itemType directory -Path $mavenPath
	New-Item -itemType directory -Path $ikeaPath"/run"

	downloadFile $url "$mavenPath\maven.zip"

	$shell=new-object -com shell.application
	$Location=$shell.namespace($mavenPath)
	$ZipFile = get-childitem "$mavenPath\maven.zip"
	$ZipFile.fullname | out-default
	$ZipFolder = $shell.namespace($ZipFile.fullname)
	$Location.Copyhere($ZipFolder.items());

	Set-Location $ikeaPath"/source";

	Invoke-Expression "git clone https://github.com/menesty/ikea.git ikea";

    Set-Location $ikeaPath;
}

$currentEnv = Get-ChildItem Env:
$pathC = $currentEnv::GetEnvironmentVariable("Path","IEUser")
#$currentEnv::GetEnvironmentVariable("Path");







