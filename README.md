# Joint_Angle
*修改于蔡神的MagNavi*

2015.06.25

啊说好的一周暴雨，我今天穿了长裤，结果闷热到不行

2015.06.26

今日实验室大整理的一番，结果只是把一堆烂箱子从这边移到那边，实验室从一进门就很乱变成进门后转个弯，妈妈呀超级乱。

由此可见整理收纳时最重要的一环是**扔**，但是哪能那么任性，毕竟实验室的东西，连一截小电线都捡起来了，我们还真是节俭~

下面是修改的一些步骤。
![](http://i.imgur.com/1YYmEJx.jpg)
这是刚改好蔡神的apk后的界面显示，数据好像一直不对，关节角度居然总是超值，和下位机液晶屏上的数字完全不相关，遂一遍一遍修改如下：

- 有个timertask在timer.schedule下200ms循环收一次数据，有挺多数据重复，大概是300ms一次
- 正常数据是三围，但是蓝牙的getinputstream在非本地这样的数据流情况下读取的都是1位+2位

![](http://i.imgur.com/Vl6qTOl.jpg)
![](http://i.imgur.com/Ucii5rA.jpg)


修改后根据时间判断，1+2之间的时间间隔较短，俩个数据包间隔长，修改好后数据基本可以是如下这样。
![](http://i.imgur.com/GhjNRFw.jpg)

