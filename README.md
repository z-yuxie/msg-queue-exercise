# msg-queue-exercise
这是一个消息队列的练习，使用了线程池和Lock，采用类似模块化的设计，并给每个订阅者也维护了一个专属于自己的消息缓存队列，可以稍作扩展便转化为一个IM系统，运行时采用在控制台输入指令的方式进行订阅者的注册、订阅、退订以及发布者发布消息等。

测试用指令:
$pushMsg 发布者1 这是一条队列中的待拉取消息1 发布者创建的队列1
$pushMsg 发布者1 这是一条队列中的待拉取消息2 发布者创建的队列1
$pushMsg 发布者2 这是一条队列中的待拉取消息3 发布者创建的队列1
$subscribeQueue 订阅者1 发布者创建的队列1
$registSubcriber 订阅者1
$subscribeQueue 订阅者1 发布者创建的队列1
$registSubcriber 订阅者2
$pushMsg 发布者2 这是一条仅有一个订阅者时的即时推送消息1 发布者创建的队列1
$subscribeQueue 订阅者2 发布者创建的队列1
$pushMsg 发布者2 这是一条有两个订阅者时的即时推送消息1 发布者创建的队列1
$subscribeQueue 订阅者2 订阅者创建的队列1
$pushMsg 发布者1 这是一条即时推送消息2 订阅者创建的队列1
$unsubscribeQueue 订阅者2 订阅者创建的队列1
$pushMsg 发布者1 这是一条即时推送消息3 订阅者创建的队列1
$unsubscribeQueue 订阅者2 发布者创建的队列1
$pushMsg 发布者1 这是一条即时推送消息4 发布者创建的队列1
$subscribeQueue 订阅者2 订阅者创建的队列1
$close
