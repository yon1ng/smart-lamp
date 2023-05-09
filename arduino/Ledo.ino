#define BLINKER_WIFI
#define BLINKER_ALIGENIE_LIGHT

#include <Blinker.h>
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

char auth[] = "";//blinker设备密钥
char ssid[] = "";//wifi ssid
char pswd[] = "";//wifi 密码


BlinkerButton Button1("btn-abc");
BlinkerButton Button2("btn-gal");
BlinkerNumber Number1("num-abc");

int counter = 0;

void button1_callback(const String &state){
  BLINKER_LOG("get button state: ", state);
  switchAllLamps();
}

void button2_callback(const String &state){
  BLINKER_LOG("get button state: ", state);
  getAllLamps();
}


void dataRead(const String &data){
  BLINKER_LOG("Blinker readString: ", data);
  counter++;
  Number1.print(counter);
}

unsigned int localPort = 41328;
char packetBuffer[2048];
String gateway;
int gatewayId;
byte removedDevices = 0x00;
byte addedDevices = 0x00;
boolean connected = false;
signed char gatewayIdArray[4];
WiFiUDP Udp;

struct lamp{
  int deviceId;  
  int red;       
  int green;     
  int blue;      
  int intensity; 
};

lamp lampArray[10];

void setup(){

  Serial.begin(115200);
  BLINKER_DEBUG.stream(Serial);
  BLINKER_DEBUG.debugAll();

  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);
  // 初始化blinker
  Blinker.begin(auth, ssid, pswd);
  Blinker.attachData(dataRead);

  Button1.attach(button1_callback);
  Button2.attach(button2_callback);

  BlinkerAliGenie.attachPowerState(aligeniePowerState);
  BlinkerAliGenie.attachColor(aligenieColor);
  BlinkerAliGenie.attachMode(aligenieMode);
  BlinkerAliGenie.attachCancelMode(aligeniecMode);
  BlinkerAliGenie.attachBrightness(aligenieBright);
  BlinkerAliGenie.attachRelativeBrightness(aligenieRelativeBright);
  BlinkerAliGenie.attachColorTemperature(aligenieColoTemp);
  BlinkerAliGenie.attachRelativeColorTemperature(aligenieRelativeColoTemp);
  BlinkerAliGenie.attachQuery(aligenieQuery);

  Udp.begin(localPort);
}

void loop(){
  Blinker.run();
  delay(10);
  if (Udp.parsePacket()){
    int len = Udp.read(packetBuffer, 2048);
    if (len > 0){
      gateway = Udp.remoteIP().toString();
      boolean removed = removedDevices != packetBuffer[21];
      boolean added = addedDevices != packetBuffer[22];
      if (connected && (removed || added))
      {
        Serial.println("LampStateChangedEvent");
        getAllLamps();
      }
      removedDevices = packetBuffer[21];
      addedDevices = packetBuffer[22];
      gatewayId = int32FromBytes(packetBuffer, 2);
      if (!connected)
      {
        connected = true;
        Serial.println("GatewayConnectedEvent");
        gatewayIdArray[0] = (gatewayId >> 24) & 0xFF;
        gatewayIdArray[1] = (gatewayId >> 16) & 0xFF;
        gatewayIdArray[2] = (gatewayId >> 8) & 0xFF;
        gatewayIdArray[3] = gatewayId & 0xFF;
        getAllLamps();
      }
    }
  }
}

void getAllLamps(){
  if (!connected || gatewayIdArray[0] == 0){
    BLINKER_LOG("gateway not connected");
    return;
  }
  char bytes[15] =
      {0xf3, 0xd4,
       gatewayIdArray[0] 
       ,
       gatewayIdArray[1] 
       ,
       gatewayIdArray[2] 
       ,
       gatewayIdArray[3] 
       ,
       0, 0, 0x1d, 0x5, 0, 0, 0, 0x43, 0};
  WiFiClient client;                      
  client.connect(gateway.c_str(), 41330); 
  if (client.connected()){
    client.write(bytes, 15);
    delay(100);
    int len = client.available();
    if (len > 0){
      char data[len];
      client.read(data, len);
      if(len > 10 && data[9] > 0){
        int dataLen = data[9];
        char data2[dataLen];
        for(int i = 0; i < dataLen; i++){
          data2[i] = data[i + 10];
        }
        saveLamps(data2, dataLen);
      }
    }
  }
  client.stop();
}

void saveLamps(char *data, int len){
  int lampCount = len / 8;
  for (int i = 0; i < lampCount; i++){
    int deviceId = int32FromBytes(data, i * 8);
    int red = data[i * 8 + 6];
    int green = data[i * 8 + 5];
    int blue = data[i * 8 + 4];
    int intensity = data[i * 8 + 7];

    lampArray[i].deviceId = deviceId;
    lampArray[i].red = red;
    lampArray[i].green = green;
    lampArray[i].blue = blue;
    lampArray[i].intensity = intensity;
    Serial.printf("deviceId %d red %d green %d blue %d intensity %d \n", deviceId, red, green, blue, intensity);
  }
}

void switchAllLamps(){
  if (!connected || gatewayIdArray[0] == 0){
    BLINKER_LOG("gateway not connected");
    return;
  }
  for (int i = 0; i < 10; i++){
    if (lampArray[i].intensity > 0){
      lampArray[i].intensity = 0;
    } else {
      lampArray[i].intensity = 0x28;
    }
  }
  updateAllLamps();
}

void updateAllLamps(){
  int lampCount = 0;
  for (int i = 0; i < 10; i++){
    if (lampArray[i].deviceId == 0){
      break;
    }
    lampCount++;
  }
  if (lampCount == 0){
    BLINKER_LOG("lampCount is 0");
    return;
  }
  int length = lampCount * 8 + 4;
  int bytesLength = lampCount * 8 + 14;
  char bytes[bytesLength] =
      {-14, -62, -1, -1, -1, -1, 0, 0, 29, lampCount * 8 + 4,0,0,0,0x43};
  int offset = 14;
  for (int i = 0; i < lampCount; i++){
    bytes[offset] = lampArray[i].deviceId & 0xFF;
    bytes[offset + 1] = (lampArray[i].deviceId >> 8) & 0xFF;
    bytes[offset + 2] = (lampArray[i].deviceId >> 16) & 0xFF;
    bytes[offset + 3] = (lampArray[i].deviceId >> 24) & 0xFF;
    bytes[offset + 4] = lampArray[i].intensity;
    bytes[offset + 5] = lampArray[i].red;
    bytes[offset + 6] = lampArray[i].green;
    bytes[offset + 7] = lampArray[i].blue;

    offset += 8;
  }
  WiFiClient client;
  client.connect(gateway.c_str(), 41330);
  if (client.connected()){
    client.write(bytes, bytesLength);
    delay(100);
    int len = client.available();
    if (len > 0){
      char data[len];
      client.read(data, len);
      BLINKER_LOG("update lamp success");
    }
  }
}

int int32FromBytes(char bytes[], int offset){
  int num = (bytes[offset] & 0xff) |
            (bytes[offset + 1] & 0xff) << 8 |
            (bytes[offset + 2] & 0xff) << 16 |
            (bytes[offset + 3] & 0xff) << 24;
  return num;
}

//以下为天猫精灵控制函数

String wsMode = BLINKER_CMD_COMMON;

void aligeniePowerState(const String & state)
{
    BLINKER_LOG("need set power state: ", state);

    if (state == BLINKER_CMD_ON) {
        BlinkerAliGenie.powerState("on");
        BlinkerAliGenie.print();
        lampArray[0].intensity = 0;
    }
    else if (state == BLINKER_CMD_OFF) {
        BlinkerAliGenie.powerState("off");
        BlinkerAliGenie.print();
        lampArray[0].intensity = 0x28;
    }
    switchAllLamps();
}

void aligenieQuery(int32_t queryCode)
{
    BLINKER_LOG("AliGenie Query codes: ", queryCode);

    switch (queryCode)
    {
        case BLINKER_CMD_QUERY_ALL_NUMBER :
            BLINKER_LOG("AliGenie Query All");
            BlinkerAliGenie.powerState(lampArray[0].intensity == 0 ? "off" : "on");
            BlinkerAliGenie.color(getColor());
            BlinkerAliGenie.mode(BLINKER_CMD_COMMON);
            BlinkerAliGenie.colorTemp(50);
            BlinkerAliGenie.brightness(lampArray[0].intensity);
            BlinkerAliGenie.print();
            break;
        case BLINKER_CMD_QUERY_POWERSTATE_NUMBER :
            BLINKER_LOG("AliGenie Query Power State");
            BlinkerAliGenie.powerState(lampArray[0].intensity == 0 ? "off" : "on");
            BlinkerAliGenie.print();
            break;
        case BLINKER_CMD_QUERY_COLOR_NUMBER :
            BLINKER_LOG("AliGenie Query Color");
            BlinkerAliGenie.color(getColor());
            BlinkerAliGenie.print();
            break;
        case BLINKER_CMD_QUERY_MODE_NUMBER :
            BLINKER_LOG("AliGenie Query Mode");
            BlinkerAliGenie.mode(BLINKER_CMD_COMMON);
            BlinkerAliGenie.print();
            break;
        case BLINKER_CMD_QUERY_COLORTEMP_NUMBER :
            BLINKER_LOG("AliGenie Query ColorTemperature");
            BlinkerAliGenie.colorTemp(50);
            BlinkerAliGenie.print();
            break;
        case BLINKER_CMD_QUERY_BRIGHTNESS_NUMBER :
            BLINKER_LOG("AliGenie Query Brightness");
            BlinkerAliGenie.brightness(lampArray[0].intensity);
            BlinkerAliGenie.print();
            break;
        default :
            BlinkerAliGenie.powerState(lampArray[0].intensity == 0 ? "off" : "on");
            BlinkerAliGenie.color(getColor());
            BlinkerAliGenie.mode(BLINKER_CMD_COMMON);
            BlinkerAliGenie.colorTemp(50);
            BlinkerAliGenie.brightness(lampArray[0].intensity);
            BlinkerAliGenie.print();
            break;
    }
}

String getColor()
{
    uint32_t color = lampArray[0].red << 16 | lampArray[0].green << 8 | lampArray[0].blue;

    switch (color)
    {
        case 0xFF0000 :
            return "Red";
        case 0xFFFF00 :
            return "Yellow";
        case 0x0000FF :
            return "Blue";
        case 0x00FF00 :
            return "Green";
        case 0xFFFFFF :
            return "White";
        case 0x000000 :
            return "Black";
        case 0x00FFFF :
            return "Cyan";
        case 0x800080 :
            return "Purple";
        case 0xFFA500 :
            return "Orange";
        default :
            return "White";
    }
}

void aligenieColor(const String & color)
{
    BLINKER_LOG("need set color: ", color);

    if (color == "Red") {
        lampArray[0].red = 255;
        lampArray[0].green = 0;
        lampArray[0].blue = 0;
    }
    else if (color == "Yellow") {
        lampArray[0].red = 255;
        lampArray[0].green = 255;
        lampArray[0].blue = 0;
    }
    else if (color == "Blue") {
        lampArray[0].red = 0;
        lampArray[0].green = 0;
        lampArray[0].blue = 255;
    }
    else if (color == "Green") {
        lampArray[0].red = 0;
        lampArray[0].green = 255;
        lampArray[0].blue = 0;
    }
    else if (color == "White") {
        lampArray[0].red = 255;
        lampArray[0].green = 255;
        lampArray[0].blue = 255;
    }
    else if (color == "Black") {
        lampArray[0].red = 0;
        lampArray[0].green = 0;
        lampArray[0].blue = 0;
    }
    else if (color == "Cyan") {
        lampArray[0].red = 0;
        lampArray[0].green = 255;
        lampArray[0].blue = 255;
    }
    else if (color == "Purple") {
        lampArray[0].red = 128;
        lampArray[0].green = 0;
        lampArray[0].blue = 128;
    }
    else if (color == "Orange") {
        lampArray[0].red = 255;
        lampArray[0].green = 165;
        lampArray[0].blue = 0;
    }

    // if (lampArray[0].intensity == 0) {
    //     lampArray[0].intensity == 0x28;
    // }

    // if (lampArray[0].intensity > 0) {
    //     lampArray[0].intensity == 0;
    // }

    updateAllLamps();

    BlinkerAliGenie.color(color);
    BlinkerAliGenie.print();
}

void aligenieMode(const String & mode)
{
    BLINKER_LOG("need set mode: ", mode);

    if (mode == BLINKER_CMD_ALIGENIE_READING) {
        // Your mode function
    }
    else if (mode == BLINKER_CMD_ALIGENIE_MOVIE) {
        // Your mode function
    }
    else if (mode == BLINKER_CMD_ALIGENIE_SLEEP) {
        // Your mode function
    }
    else if (mode == BLINKER_CMD_ALIGENIE_HOLIDAY) {
        // Your mode function
    }
    else if (mode == BLINKER_CMD_ALIGENIE_MUSIC) {
        // Your mode function
    }
    else if (mode == BLINKER_CMD_ALIGENIE_COMMON) {
        // Your mode function
    }

    wsMode = mode;

    BlinkerAliGenie.mode(mode);
    BlinkerAliGenie.print();
}

void aligeniecMode(const String & cmode)
{
    BLINKER_LOG("need cancel mode: ", cmode);

    if (cmode == BLINKER_CMD_ALIGENIE_READING) {
        // Your mode function
    }
    else if (cmode == BLINKER_CMD_ALIGENIE_MOVIE) {
        // Your mode function
    }
    else if (cmode == BLINKER_CMD_ALIGENIE_SLEEP) {
        // Your mode function
    }
    else if (cmode == BLINKER_CMD_ALIGENIE_HOLIDAY) {
        // Your mode function
    }
    else if (cmode == BLINKER_CMD_ALIGENIE_MUSIC) {
        // Your mode function
    }
    else if (cmode == BLINKER_CMD_ALIGENIE_COMMON) {
        // Your mode function
    }

    wsMode = BLINKER_CMD_COMMON; // new mode

    BlinkerAliGenie.mode(wsMode); // must response
    BlinkerAliGenie.print();
}

void aligenieBright(const String & bright)
{
    BLINKER_LOG("need set brightness: ", bright);

    if (bright == BLINKER_CMD_MAX) {
        lampArray[0].intensity = 0xff;
    }
    else if (bright == BLINKER_CMD_MIN) {
        lampArray[0].intensity = 0;
    }
    else {
        lampArray[0].intensity = bright.toInt();
    }

    BLINKER_LOG("now set brightness: ", lampArray[0].intensity);

    updateAllLamps();

    BlinkerAliGenie.brightness(lampArray[0].intensity);
    BlinkerAliGenie.print();
}

void aligenieRelativeBright(int32_t bright)
{
    BLINKER_LOG("need set relative brightness: ", bright);

    if (lampArray[0].intensity + bright < 255 && lampArray[0].intensity + bright >= 0) {
        lampArray[0].intensity += bright;
    }

    BLINKER_LOG("now set brightness: ", lampArray[0].intensity);

    updateAllLamps();

    BlinkerAliGenie.brightness(bright);
    BlinkerAliGenie.print();
}

void aligenieColoTemp(int32_t colorTemp)
{
    BLINKER_LOG("need set colorTemperature: ", colorTemp);

    BlinkerAliGenie.colorTemp(colorTemp);
    BlinkerAliGenie.print();
}

void aligenieRelativeColoTemp(int32_t colorTemp)
{
    BLINKER_LOG("need set relative colorTemperature: ", colorTemp);

    BlinkerAliGenie.colorTemp(colorTemp);
    BlinkerAliGenie.print();
}
