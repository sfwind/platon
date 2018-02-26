package com.iquanwai.platon.biz.domain.operation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.LiveRedeemCodeDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.fragmentation.manager.CardManager;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.material.UploadResourceService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.LiveRedeemCode;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.WechatMessage;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.ImageUtils;
import com.iquanwai.platon.biz.util.PromotionConstants;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by nethunder on 2017/8/30.
 */
@Service
public class TheatreServiceImpl implements TheatreService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String CURRENT_GAME = PromotionConstants.Activities.CAITONG_LIVE;

    public static final String CODE_DESCRIBE_URL = "<a href='https://shimo.im/doc/lOBVOFcT2z40qI3Q?r=NPGKQE/'>石墨文档</a>";
    public static final String CODE_CHANGE_URL = "<a href='http://m.study.163.com/myCoupon'>兑换地址</a>";


    public interface CURRENT_ACTION extends PromotionConstants.CaitongLiveAction {
    }

    private static final String BACKPACK = "背包";

    private TheatreScript theatreScript;


    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private PromotionActivityDao promotionActivityDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private LiveRedeemCodeDao liveRedeemCodeDao;
    @Autowired
    private LiveRedeemCodeRepository liveRedeemCodeRepository;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private UploadResourceService uploadResourceService;
    @Autowired
    private CardManager cardRepository;


    @PostConstruct
    public void initScript() {
        theatreScript = new TheatreScript();
        theatreScript.setPrologue("圈圈闭关两周一直没有更文，你只好每天去阅览室里翻找圈外的精华文章。\n\n" +
                "这天你刚刚从圈外图书馆走出来，就发现有三个人站在图书馆门口偷偷策划着什么不为人知的大事情。");
        theatreScript.setEndingWords("你念出了正确的咒语，霸王龙的骨架慢慢停止了前进，又恢复成了一个展览品。采铜馆长拍拍你的肩膀，指了指前面的宝箱，示意你过去拿。{split}" +
                "你跑到宝箱面前，颤抖地打开了箱子，发现里面有一张纸条：只有真正的勇士才配得上我的直播，下个月我等你来——傅踢踢。\n" +
                "----------------------\n" +
                "你已被系统记录为【真正的勇士】，下个月的博物馆奇妙夜你将被踢踢邀请出席，请记得关注【圈外同学】服务号收取通知。\n" +
                "----------------------\n" +
                "P.S：傅踢踢：公众号「傅踢踢」创始人，情感作家里为数不多的直男{split}" +
                "你拿到了宝藏，但是你猛地回头，发现采铜已经消失在出口的尽头。\n" +
                "----------------------\n" +
                "恭喜你，你已经完成了本次探险，如果之前没有领取到兑换码和邀请券的勇士可以回复【背包】查看你的兑换码和邀请券。");

        theatreScript.addQuestion("圈圈还在闭关中，这三个人就在这里偷偷搞事情！你决定一探究竟，一路跟着他们来到了圈外博物馆。\n\n" +
                        "在你打算直接跨入大门之时，两个守卫突然现身、拦住了你，声称你带了某件【圈外博物馆】的违禁品。你说：“我去，竟然不让带…… ” （请选出不让带的东西） \n" +
                        "--------------------",
                CURRENT_ACTION.QUESTION1)
                .addAnswer(11, "圈圈写的《请停止无效努力》一书", "守卫立马对小黄书鞠了一躬，毕竟小黄书是逆袭圣经啊！怎么可能是违禁物品？\n" +
                        "----------------------\n" +
                        "很抱歉，你连博物馆的门都没进去就失败了，不过你可以回复【数字编号】继续闯关\n\n" +
                        "【{replayKey}】重新挑战\n" +
                        "【{closeKey}】结束挑战\n", 1)
                .addAnswer(12, "能上【圈外同学】课程的手机", "守卫掏出了自己的课程手机上起了【圈外同学】课程课程，说：“哥们，一起来！”\n" +
                        "----------------------\n" +
                        "很抱歉，你连博物馆的门都没进去就失败了，不过你可以回复【数字编号】继续闯关\n\n" +
                        "【{replayKey}】重新挑战\n" +
                        "【{closeKey}】结束挑战\n", 2)
                .addAnswer(13, "能打各类游戏的小霸王游戏机", null, null);
        theatreScript.addQuestion(
                "你成功为了圈圈舍弃了你最爱的小霸王学习机，这种精神值得被鼓励，我们决定免费送你一次【圈外博物馆的直播分享】。（兑换码已经放入你的背包里了，在游戏结束时可以回复【背包】查看领取）\n\n" +
                        "现在请你继续往下走，还有两个神秘宝藏等着你。\n\n" +
                        "刚进入了博物馆的大门，前面有一个老大爷，站在博物馆楼前，他看着你说：“找到圈外博物馆首任馆长，他知道你要找的人是谁。”\n\n" +
                        "你推开门，前方是一个看不见尽头的走廊，走廊的左手边挂着一排人物画像。你要在这里找到首任馆长的画像，于是你走向了：\n" +
                        "--------------------",
                CURRENT_ACTION.QUESTION2)
                .addAnswer(21, "孙圈圈的画像", "圈圈开始给你讲解结构化思维，一开口就再也没有停下\n" +
                        "----------------------\n" +
                        "你听着圈圈的声音昏昏欲睡，不过你可以回复【数字编号】继续闯关\n\n" +
                        "【{replayKey}】摆脱圈圈的控制\n" +
                        "【{closeKey}】结束挑战\n", 3)
                .addAnswer(22, "马徐骏的画像", null, null)
                .addAnswer(23, "特朗普的画像", "特朗普睁开了眼睛，并骂了你一句：“I will build a wall between us and let you pay for it！”\n" +
                        "----------------------\n" +
                        "你高声喊道：Hail QuanQuan！，不过你可以回复【数字编号】继续闯关\n\n" +
                        "【{replayKey}】就可以重新挑战\n" +
                        "【{closeKey}】结束挑战\n", 4);
        theatreScript.addQuestion(
                "你对着马徐骏的画像念了一段咒语：“马馆长好帅！阿哩啊哩！”画像逐渐变亮。。。\n\n" +
                        "马馆长被你成功唤醒了，作为奖励，你得到了一个价值264元的免费邀请券，能够让你的三位小伙伴免费收听本次直播分享。（邀请券已经放入你的背包里了，游戏结束时可以回复【背包】查看领取）\n\n" +
                        "马徐骏馆长缓缓睁开眼睛，张口来了一段freestyle，你一边听着他的freestyle，一边想着怎么找到下一步的线索。你应该怎么说：\n" +
                        "--------------------",
                CURRENT_ACTION.QUESTION3)
                .addAnswer(31, "你的梦想是什么？", "马徐骏说完他的梦想之后闭上了眼睛，于是你还是不知道要怎么走出这个房间\n" +
                        "----------------------\n" +
                        "你迷失在这个走廊中，不过你可以回复【数字编号】继续闯关\n\n" +
                        "【{replayKey}】再次挑战\n" +
                        "【{closeKey}】结束挑战\n", 5)
                .addAnswer(32, "马老师给我签个名！", "马徐骏告诉你藏有他签名照的柜子在哪里，但是你还是不知道怎么走出这个房间\n" +
                        "----------------------\n" +
                        "你迷失在这个走廊中，不过你可以回复【数字编号】继续闯关\n\n" +
                        "【{replayKey}】再次挑战\n" +
                        "【{closeKey}】结束挑战\n", 6)
                .addAnswer(33, "马老师你有没有看到有三个可疑的人走进博物馆？", null, null);
        theatreScript.addQuestion(
                "马徐骏馆长用眼神示意你了一个方向，并帮你打开了通往下一个房间的大门。原来大门就藏在画像背后。\n\n" +
                        "得到马徐骏馆长的指点，你推开第一扇门，发现这里竟然放着泰坦尼克号的模型。\n\n" +
                        "当你从旁边经过的时候，Jack从水里冒了出来：年轻人，想要过去，先回答我一个问题，我已经在水下思考几百年了，你知道什么是\"冰山模型\"吗？\n" +
                        "--------------------",
                CURRENT_ACTION.QUESTION4)
                .addAnswer(41, "我在圈外同学课程里看到过：冰山模型全面地描述了一个人所有的内在价值要素", null, null)
                .addAnswer(42, "冰山模型和泰坦尼克号的模型原本是一对", "原来你也不知道啊，那你下来陪我一起想吧（你被拉入了水中）\n" +
                        "----------------------\n" +
                        "你被Jack拖入水中，不过你可以回复【数字编号】继续闯关\n\n" +
                        "【{replayKey}】再次挑战\n" +
                        "【{closeKey}】结束挑战\n", 7)
                .addAnswer(43, "不理他，直接往前走", "你发现越走越冷，回过神来发现自己竟然已经走到了水中。。。\n" +
                        "----------------------\n" +
                        "你被Jack拖入水中，不过你可以回复【数字编号】继续闯关\n\n" +
                        "【{replayKey}】再次挑战\n" +
                        "【{closeKey}】结束挑战\n", 8);
        theatreScript.addQuestion(
                "“啊！原来学过圈外课程的人都知道”，Jack长叹一口气，慢慢地沉了下去。。。你继续往前走\n\n" +
                        "摆脱了Jack的你继续往前走，几分钟后发现前面有一扇长满青苔的石门。\n\n" +
                        "你隐隐约约听到门的背后有一男一女正在说话，你急忙想推开门，可是发现怎么也推不开，你发现门上有一行字：说出圈外正在打造什么？\n" +
                        "--------------------",
                CURRENT_ACTION.QUESTION5)
                .addAnswer(51, "圈外托儿所", "你大声喊出：圈外托儿所，门丝毫未动，无论你怎么敲打都打不开\n" +
                        "----------------------\n" +
                        "你失败了，不过你可以回复【数字编号】继续闯关\n\n" +
                        "【{replayKey}】再次挑战\n" +
                        "【{closeKey}】结束挑战\n", 9)
                .addAnswer(52, "圈外出版社", "你大声喊出：圈外出版社，门丝毫未动，无论你怎么敲打都打不开\n" +
                        "----------------------\n" +
                        "你失败了，不过你可以回复【数字编号】继续闯关\n\n" +
                        "【{replayKey}】再次挑战\n" +
                        "【{closeKey}】结束挑战\n", 10)
                .addAnswer(53, "圈外商学院", null, null);
        theatreScript.addQuestion("石门上的字慢慢消退，你听到了石锁转动发出沉闷的声音。门开了，你走了进去。正当你走过石门的时候，石门缓缓地说道：我偷偷告诉你一个秘密吧，注意关注最近几天的圈外信息，圈圈院长好像在搞什么大事情。。。 \n\n" +
                        "你听了感觉非常疑惑，但是你已经没时间思考这些了。你急匆匆的跑上前去，你只在前面看到了一位穿着斗篷的男子，那个女子已经不见踪影。\n\n" +
                        "你上前询问，得知这位男子就是采铜。这时，那男子问你：“你知道我的江湖名号吗？”\n" +
                        "--------------------",
                CURRENT_ACTION.QUESTION6)
                .addAnswer(61, "挖矿的？难道你就是传说中的比特币大亨！", "这位男子听到答案哈哈大笑，你以为自己猜对了，却发现神秘男子已经消失不见\n" +
                        "----------------------\n" +
                        "没办法，谁叫你说错话了呢？回复\n\n" +
                        "【{replayKey}】再次挑战\n" +
                        "【{closeKey}】结束挑战\n", 11)
                .addAnswer(62, "知乎心理大v，畅销书《精进》作者", null, null)
                .addAnswer(63, "音乐大神，刚在网易云音乐发布了新的音乐专辑", "小伙子，很有想法嘛，和我学Freestyle吧。\n" +
                        "----------------------\n" +
                        "于是，你和采铜老师在Freestyle的路上越走越远，回复\n\n" +
                        "【{replayKey}】再次挑战\n" +
                        "【{closeKey}】结束挑战\n", 12);
        theatreScript.addQuestion(
                "这位神秘男子向你走来，嘴角露出了神秘的微笑：D\n\n" +
                        "你好不容易答对前一个问题，那个叫采铜的男子又问了你一个问题：“你知道为什么我们叫圈外博物馆吗？”你该怎么回答？     \n" +
                        "--------------------",
                CURRENT_ACTION.QUESTION7)
                .addAnswer(71, "你们收藏了很多历史文物", "采铜馆长对这个回答并不满意，长叹一声消失在走廊的尽头\n" +
                        "----------------------\n" +
                        "别说了，回复\n\n" +
                        "【{replayKey}】再次挑战\n" +
                        "【{closeKey}】结束挑战\n", 13)
                .addAnswer(72, "你们希望通过12次大咖分享直播提升职场人的学习能力和视野", null, null)
                .addAnswer(73, "因为博物馆一听就很有文化", "采铜馆长对这个回答并不满意，长叹一声消失在走廊的尽头\n" +
                        "----------------------\n" +
                        "别说了，回复\n\n" +
                        "【{replayKey}】再次挑战\n" +
                        "【{closeKey}】结束挑战\n", 14);
        theatreScript.addQuestion(
                "嗯，看来你准备的很充分啊，跟我走吧，采铜向你挥挥手。\n\n" +
                        "你这才知道采铜就是圈外博物馆的特邀馆长。采铜馆长一边走一边和你说关于这次圈圈搞得大事情。\n\n" +
                        "突然这个时候，在阴暗角落冲出了一只怒吼中的霸王龙，在强大的恐龙面前，你的战斗力太弱了，这时你想：\n" +
                        "--------------------",
                CURRENT_ACTION.QUESTION8)
                .addAnswer(81, "果然不上圈外同学课程，战斗力跟不上啊", null, null)
                .addAnswer(82, "你后退一步，把采铜馆长推了出去", "霸王龙就像没看到采铜馆长一般，直向你奔来，一尾巴把你甩飞到墙上，抠都抠不下来\n" +
                        "----------------------\n" +
                        "回复\n\n" +
                        "【{replayKey}】可以召唤蓝翔挖掘机把你从墙上挖下来\n" +
                        "【{closeKey}】结束挑战\n", 15)
                .addAnswer(83, "你转头撒腿就跑，留给采铜馆长一个潇洒的背影", "你永远失去了得到珍宝的机会！\n" +
                        "----------------------\n" +
                        "【{replayKey}】重新挑战\n" +
                        "【{closeKey}】结束挑战\n", 16);
        theatreScript.addQuestion("你意识到了自己因为没上圈外同学课程而战斗力不足，懊恼不已。\n\n" +
                        "采铜馆长好像看穿了你的想法并用意念对你说：“去那边3本书里挑一本我编写的秘法宝典，这个可以大幅度增加你的战斗力。”\n\n" +
                        "你听完以后火速地跑过去，选出了这本书：\n" +
                        "--------------------",
                CURRENT_ACTION.QUESTION9)
                .addAnswer(91, "《请停止无效努力》", "这不就是我要交给门卫的小黄书嘛！你津津有味地读了起来，忘记了自己要做什么。\n" +
                        "----------------------\n" +
                        "回复\n\n" +
                        "【{replayKey}】原地复活\n" +
                        "【{closeKey}】结束挑战\n", 17)
                .addAnswer(92, "《精进:如何成为一个很厉害的人》", null, null)
                .addAnswer(93, "《毛泽东思想和中国特色社会主义理论体系概论》", "这个时候拿到了这本书，你的心里仿佛有几百万只草泥马呼啸而过。。\n" +
                        "----------------------\n" +
                        "你被自己心里的草泥马踩死了。。回复 \n\n" +
                        "【{replayKey}】原地复活\n" +
                        "【{closeKey}】结束挑战\n", 18);
        theatreScript.addQuestion("拿起书的一瞬间，封面上的乌鸦通体发亮，吓得霸王龙倒退了几步。你知道自己选对了！\n\n" +
                        "你翻开书，发现里面一共有三条咒语，你选对了以后真的战斗力提升了一大截。\n" +
                        "--------------------",
                CURRENT_ACTION.QUESTION10)
                .addAnswer(101, "用持续精确的努力，撬动更大的可能，这便是精进！", null, null)
                .addAnswer(102, "稳住，我们能赢！", "你找错了咒语，你被霸王龙一脚踢出了博物馆，你口中大喊：我还会回来的。。\n" +
                        "----------------------\n" +
                        "【{replayKey}】原地复活\n" +
                        "【{closeKey}】结束挑战\n", 19)
                .addAnswer(103, "发现本质问题，减少无效努力", "这个咒语看上去很正确，但是只对霸王龙造成了一点伤害，你被霸王龙一口吃了下去。\n" +
                        "----------------------\n" +
                        "【{replayKey}】原地复活\n" +
                        "【{closeKey}】结束挑战\n", 20);
        theatreScript.initReplayKey();

    }

    /**
     * 处理背包信息,计算礼物等级
     *
     * @param profile 用户信息
     */
    private void handleBackpackMessage(Profile profile) {
        // 查看他的直播优惠码
        LiveRedeemCode liveRedeemCode = liveRedeemCodeDao.loadLiveRedeemCode(CURRENT_GAME, profile.getId());
        if (liveRedeemCode == null) {
            customerMessageService.sendCustomerMessage(profile.getOpenid(), "您回复了背包,不过暂时还没有兑换码", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        } else {
            // 查看做的题目
            this.sendCodeToUser(profile, liveRedeemCode);
            Pair<Boolean, Question> questionPair = this.loadMaxPlayQuestion(profile.getId());
            Question maxPlayQuestion = questionPair != null ? questionPair.getRight() : null;
            if (maxPlayQuestion != null && maxPlayQuestion.getAction() >= CURRENT_ACTION.QUESTION3) {
                // 做到第四题才会有邀请券,发海报
                customerMessageService.sendCustomerMessage(profile.getOpenid(), "下方是你的邀请券，通过你的邀请券进来的朋友享受“勇士の朋友”特殊待遇，可以免费听本次直播哦。", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                String mediaId = generateSharePage(profile);
                customerMessageService.sendCustomerMessage(profile.getOpenid(), mediaId, Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
            }
        }
    }

    /**
     * 完成到最大的题目
     *
     * @param profileId 用户id
     * @return left-是否goDie  right-问题
     */
    private Pair<Boolean, Question> loadMaxPlayQuestion(Integer profileId) {

        PromotionActivity activity = promotionActivityDao.loadMaxPlayQuestion(profileId, CURRENT_GAME, CURRENT_ACTION.BACKPACK);
        if (activity == null) {
            return null;
        }

        PromotionActivity deadAction = promotionActivityDao.loadDeadQuestion(profileId, CURRENT_GAME, CURRENT_ACTION.GO_DIE);
        Boolean isDead = deadAction != null && deadAction.getAddTime().after(activity.getAddTime());
        Question question = theatreScript.searchQuestionByAction(activity.getAction());
        return new MutablePair<>(isDead, question);
    }

    /**
     * 进行中的最后的的题目
     *
     * @param profileId 用户id
     * @return left-是否goDie  right-问题
     */
    private Pair<Boolean, Question> loadLastPlayQuestion(Integer profileId) {

        PromotionActivity activity = promotionActivityDao.loadLastPlayQuestion(profileId, CURRENT_GAME, CURRENT_ACTION.BACKPACK);
        if (activity == null) {
            logger.error("用户:{} 还未开始答题");
            return null;
        }

        PromotionActivity deadAction = promotionActivityDao.loadDeadQuestion(profileId, CURRENT_GAME, CURRENT_ACTION.GO_DIE);
        Boolean isDead = deadAction != null && deadAction.getAddTime().getTime() >= activity.getAddTime().getTime();
        Question question = theatreScript.searchQuestionByAction(activity.getAction());
        return new MutablePair<>(isDead, question);
    }

    /**
     * 处理重玩逻辑
     *
     * @param profile      用户信息
     * @param lastQuestion 当前在做的question
     * @param key          输入的key
     */
    private void handleReplay(Profile profile, Question lastQuestion, Integer key) {
        if (lastQuestion == null) {
            // 之前没有做题，推送第一道题目
            Question firstQuestion = theatreScript.firstQuestion();
            this.sendQuestionToUser(profile, firstQuestion);
            return;
        }
        // 输入的应该是Question
        Question wannaQuestion = theatreScript.searchQuestionByKey(key);
        if (wannaQuestion == null) {
            customerMessageService.sendCustomerMessage(profile.getOpenid(), "你输入的编号和你正在做的题目不对应哦", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        } else {
            if (lastQuestion.getAction() >= wannaQuestion.getAction()) {
                // 之前做的题目比这个靠后，可以重做,推送题目
                this.sendQuestionToUser(profile, wannaQuestion);
            } else {
                // 之前做的题目比这个靠前，不可以重做
                this.sendQuestionToUser(profile, lastQuestion);
            }
        }
    }


    /**
     * 处理答题逻辑
     *
     * @param profile  用户信息
     * @param question 题目信息
     * @param answer   答案信息
     */
    private void handleAnswer(Profile profile, Question question, Answer answer) {
        if (answer.getBadEnding()) {
            // go die
            PromotionActivity completeAction = new PromotionActivity();
            completeAction.setAction(CURRENT_ACTION.GO_DIE);
            completeAction.setActivity(CURRENT_GAME);
            completeAction.setProfileId(profile.getId());
            promotionActivityDao.insertPromotionActivity(completeAction);
            customerMessageService.sendCustomerMessage(profile.getOpenid(), "【Bad Ending " + answer.getBadEndingSequence() + "】\n\n" + answer.getDeadWords(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        } else {
            // go live 回答正确
            if (theatreScript.isLastQuestion(question)) {
                // 最后一道题回答正确，送奖品
                PromotionActivity completeAction = new PromotionActivity();
                completeAction.setAction(CURRENT_ACTION.COMPLETE);
                completeAction.setActivity(CURRENT_GAME);
                completeAction.setProfileId(profile.getId());
                promotionActivityDao.insertPromotionActivity(completeAction);
                // 送出礼物，延后到发背包的时候
                String[] split = theatreScript.getEndingWords().split("\\{split\\}");
                for (String str : split) {
                    customerMessageService.sendCustomerMessage(profile.getOpenid(), str, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                }
            } else {
                // 不是最后一题，推送下一题
                Question nextQuestion = theatreScript.nextQuestion(question);
                if (nextQuestion == null) {
                    logger.error("异常，没有下一题了,{}", question);
                } else {
                    if (CURRENT_ACTION.QUESTION5 == question.getAction()) {
                        // 第五题回答正确，推送采铜图片
                        this.sendCaitongHeadToUser(profile);
                    }
                    this.sendQuestionToUser(profile, nextQuestion);
                }
            }
            // 第一题会送一个兑换码
            if (theatreScript.isFirstQuestion(question)) {
                LiveRedeemCode liveRedeemCode = liveRedeemCodeRepository.useLiveRedeemCode(CURRENT_GAME, profile.getId());
                logger.info("送出兑换码:{}", liveRedeemCode);
            }
        }
    }


    /**
     * 向用户发送题目
     *
     * @param profile  用户
     * @param question 题目
     */
    private void sendQuestionToUser(Profile profile, Question question) {
        PromotionActivity completeAction = new PromotionActivity();
        completeAction.setAction(question.getAction());
        completeAction.setActivity(CURRENT_GAME);
        completeAction.setProfileId(profile.getId());
        promotionActivityDao.insertPromotionActivity(completeAction);
        String str = "【第" + (question.getAction() - 30) + "/10关】\n\n";
        StringBuilder message = new StringBuilder(str).append(question.getWords()).append("\n")
                .append("请回复【数字编号】继续闯关，赢取神秘宝藏\n");
        List<Answer> answers = question.getAnswerList();
        answers.forEach(answer -> {
            message.append("\n")
                    .append("【").append(answer.getKey()).append("】").append(" ").append(answer.getWords());
        });

        if (question.getAction().equals(CURRENT_ACTION.QUESTION1)) {
            // 第一题先推送开场白
            customerMessageService.sendCustomerMessage(profile.getOpenid(), theatreScript.getPrologue(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        }

        customerMessageService.sendCustomerMessage(profile.getOpenid(), message.toString(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

    /**
     * 发送游戏结束的信息给用户
     *
     * @param profile      用户
     * @param lastQuestion 最后一道题目
     */
    private void sendGameOverToUser(Profile profile, Question lastQuestion, String message) {
        if ("结束".equals(message)) {
            // 确定结束，查看最后一个action是不是closeGame
            PromotionActivity closeGame = promotionActivityDao.loadLastAction(profile.getId(), CURRENT_GAME);
            if (closeGame.getAction().equals(CURRENT_ACTION.CLOSE_TIP)) {
                // 确定结束
                PromotionActivity closeAction = new PromotionActivity();
                closeAction.setAction(CURRENT_ACTION.CLOSE_GAME);
                closeAction.setActivity(CURRENT_GAME);
                closeAction.setProfileId(profile.getId());
                promotionActivityDao.insertPromotionActivity(closeAction);
                // 查询兑换码
                LiveRedeemCode liveRedeemCode = liveRedeemCodeDao.loadLiveRedeemCode(CURRENT_GAME, profile.getId());
                if (liveRedeemCode == null) {
                    logger.info("用户未获得兑换码就结束游戏:{}", profile.getId());
                    String stringBuilder = "你已经挑战过了圈外博物馆啦，但很可惜你失败了。\n" +
                            "快点学一些圈外课程提升自己的战斗力吧！";
                    customerMessageService.sendCustomerMessage(profile.getOpenid(), stringBuilder, Constants.WEIXIN_MESSAGE_TYPE.TEXT);

                } else {
                    this.sendCodeToUser(profile, liveRedeemCode);
                    Pair<Boolean, Question> questionPair = this.loadMaxPlayQuestion(profile.getId());
                    Question maxPlayQuestion = questionPair != null ? questionPair.getRight() : null;
                    if (maxPlayQuestion != null && maxPlayQuestion.getAction() >= CURRENT_ACTION.QUESTION3) {
                        // 做到第四题才会有邀请券,发海报
                        customerMessageService.sendCustomerMessage(profile.getOpenid(), "下方是你的邀请券，通过你的邀请券进来的朋友享受“勇士の朋友”特殊待遇，可以免费听本次直播哦。", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                        String mediaId = generateSharePage(profile);
                        customerMessageService.sendCustomerMessage(profile.getOpenid(), mediaId, Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
                    }
                }
            }
        } else {
            // 插入询问
            PromotionActivity closeAction = new PromotionActivity();
            closeAction.setAction(CURRENT_ACTION.CLOSE_TIP);
            closeAction.setActivity(CURRENT_GAME);
            closeAction.setProfileId(profile.getId());
            promotionActivityDao.insertPromotionActivity(closeAction);
            String stringBuilder = "您确认要结束闯关，离开博物馆嘛？\n" +
                    "----------------------\n" +
                    "请回复【编号】：\n\n" +
                    "【结束】您将放弃神秘宝藏\n" +
                    "【{replayKey}】再次挑战\n".replace("{replayKey}", lastQuestion.getReplayKey().toString());
            customerMessageService.sendCustomerMessage(profile.getOpenid(), stringBuilder, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        }
    }


    @Override
    public void handleTheatreMessage(WechatMessage wechatMessage) {
        logger.info("handleTheatreMessage");
        String message = wechatMessage.getMessage();
        Profile profile = accountService.getProfile(wechatMessage.getOpenid());
        // 获取最后一进行的一个题目
        Pair<Boolean, Question> questionPair = this.loadLastPlayQuestion(profile.getId());
        // 玩过的最后一题
        Question question = questionPair != null ? questionPair.getRight() : null;
        // 是否game over
        Boolean isDead = questionPair != null ? questionPair.getLeft() : false;

        if (message.equals(BACKPACK)) {
            // 处理背包逻辑
            this.handleBackpackMessage(profile);
        } else if ("00".equals(message) || "结束".equals(message)) {
            // 要结束游戏
            if (question != null && isDead) {
                this.sendGameOverToUser(profile, question, message);
            } else {
                logger.error("用户：{},没有结束，但是输入了【00/结束 】", profile.getId());
            }
        } else {
            if (StringUtils.isNumeric(message)) {
                Integer key = Integer.parseInt(message);
                // 这个key能否对应这个question的答案
                Answer answer = question != null ? question.searchAnswerByKey(key) : null;
                if (answer == null) {
                    // 处理回复逻辑
                    this.handleReplay(profile, question, key);
                } else if (!isDead) {
                    // 处理答题逻辑,这道题没有死
                    this.handleAnswer(profile, question, answer);
                }
                // badEnding必须回复题目，而不是答案
            }
        }
        // 输入的不是背包，也不是数字，ignore
    }


    @Override
    public Boolean isPlayingTheatre(WechatMessage wechatMessage) {
        Profile profile = accountService.getProfile(wechatMessage.getOpenid());
        PromotionLevel liveLevel = promotionLevelDao.loadByProfileId(profile.getId(), CURRENT_GAME);
        if (liveLevel != null) {
            // 扫码，但是要判断是否结束了游戏
            PromotionActivity closeAction = promotionActivityDao.loadAction(profile.getId(), CURRENT_GAME, CURRENT_ACTION.CLOSE_GAME);
            if (closeAction == null) {
                // 没有结束，检查是第几层
                if (liveLevel.getLevel() == 1) {
                    // 第一层可以直接开始
                    return true;
                } else {
                    // 有没有手动输入开始
                    PromotionActivity manualStart = promotionActivityDao.loadAction(profile.getId(), CURRENT_GAME, CURRENT_ACTION.MANUAL_START);
                    if (manualStart == null) {
                        // 没有输入手动开始
                        String message = wechatMessage.getMessage();
                        if ("48".equals(message)) {
                            PromotionActivity manualStartAction = new PromotionActivity();
                            manualStartAction.setProfileId(profile.getId());
                            manualStartAction.setAction(CURRENT_ACTION.MANUAL_START);
                            manualStartAction.setActivity(CURRENT_GAME);
                            promotionActivityDao.insertPromotionActivity(manualStartAction);
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    @Override
    public void startGame(Profile profile) {
        // 检查用户是否结束了游戏
        PromotionActivity closeGame = promotionActivityDao.loadAction(profile.getId(), CURRENT_GAME, CURRENT_ACTION.CLOSE_GAME);
        if (closeGame != null) {
            // 用户游戏已关闭
            LiveRedeemCode liveRedeemCode = liveRedeemCodeDao.loadLiveRedeemCode(CURRENT_GAME, profile.getId());
            if (liveRedeemCode == null) {
                // 没有获得
                String message = "你已经挑战过了圈外博物馆啦，但很可惜你失败了。\n" +
                        "快点学一些圈外课程提升自己的战斗力吧！";
                customerMessageService.sendCustomerMessage(profile.getOpenid(), message, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            } else {
                // 获得过直播券
                this.sendCodeToUser(profile, liveRedeemCode);
            }
        } else {
            // 由mq发起，那里会插入level，所以不检查了
            PromotionActivity startAction = new PromotionActivity();
            startAction.setAction(CURRENT_ACTION.QUESTION1);
            startAction.setActivity(CURRENT_GAME);
            startAction.setProfileId(profile.getId());
            promotionActivityDao.insertPromotionActivity(startAction);
            // 发送开场白以及第一题文案:
            // 不推送开场白
//            customerMessageService.sendCustomerMessage(profile.getOpenId(), theatreScript.getPrologue(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            // 发送第一题文案
            Question firstQuestion = theatreScript.firstQuestion();
            this.sendQuestionToUser(profile, firstQuestion);
        }
    }

    private String generateSharePage(Profile profile) {
        String scene = CURRENT_GAME + "_" + profile.getId();
        BufferedImage qrBuffer = qrCodeService.loadQrImage(scene);
        BufferedImage newCodeBuffer = ImageUtils.scaleByPercentage(qrBuffer, 174, 174);
        BufferedImage caitongBG = cardRepository.loadCaitongBgImage();
        BufferedImage result = ImageUtils.overlapImage(caitongBG, newCodeBuffer, 38, 1124);
        return uploadResourceService.uploadResource(result);
    }

    private void sendCaitongHeadToUser(Profile profile) {
        BufferedImage bufferedImage = cardRepository.loadCaitongHead();
        String mediaId = uploadResourceService.uploadResource(bufferedImage);
        customerMessageService.sendCustomerMessage(profile.getOpenid(), mediaId, Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
    }


    @Override
    public void sendCodeToUser(Profile profile, LiveRedeemCode liveRedeemCode) {
        String message1 = "下方是你的兑换码，可以免费兑换，报名参加采铜馆长的直播课（售价88元）\n\n" +
                "↓兑换码↓（长按复制）";
        customerMessageService.sendCustomerMessage(profile.getOpenid(), message1, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        customerMessageService.sendCustomerMessage(profile.getOpenid(), liveRedeemCode.getCode(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        String message2 = "【直播兑换码使用说明】\n\n" +
                "直播时间：9月21日20：30\n" +
                "直播价格：88元（使用兑换码免费）\n" +
                "兑换地址：" + CODE_CHANGE_URL + "\n" +
                "兑换说明：" + CODE_DESCRIBE_URL;
        customerMessageService.sendCustomerMessage(profile.getOpenid(), message2, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

    @Override
    public void sendLiveCode(String openId) {
        Profile profile = accountService.getProfile(openId);
        String msg1 = "【大咖直播限时免费】\n" +
                "\n" +
                "昨天很多同学已经猜到那个神秘的男子是采铜老师啦。没错，我们邀请到了畅销书《精进》作者采铜老师来为大家做直播分享\n" +
                "------------------\n" +
                "但是，兑换码小哥哥已经发光了，我又特地申请圈圈给大家弄了一个限时免费页面【9月20日截止】\n" +
                "------------------\n" +
                "直接点击下面的链接就可以报名了（直接使用微信登录，无需下载App）";
        customerMessageService.sendCustomerMessage(profile.getOpenid(), msg1, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        String msg2 = "<a href='http://study.163.com/topics/quanwaicaitong/?from=singlemessage&isappinstalled=0'>点我报名</a>";
        customerMessageService.sendCustomerMessage(profile.getOpenid(), msg2, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }
}


/**
 * 剧本
 */
@Data
class TheatreScript {
    private String prologue;
    private String endingWords;
    private List<Question> questionList;

    TheatreScript() {
        this.questionList = Lists.newArrayList();
    }

    /**
     * 查看是否是最后一个question
     *
     * @param question 问题
     * @return 是否最后一个
     */
    Boolean isLastQuestion(Question question) {
        if (CollectionUtils.isEmpty(this.questionList)) {
            return false;
        }
        return this.questionList.get(this.questionList.size() - 1) == question;
    }

    /**
     * 查看是否第一个问题
     *
     * @param question 问题
     * @return 是否第一个
     */
    Boolean isFirstQuestion(Question question) {
        if (CollectionUtils.isEmpty(this.questionList)) {
            return false;
        }
        return this.questionList.get(0) == question;
    }

    /**
     * 添加问题
     *
     * @param words  文本
     * @param action Activity表的actionn
     * @return 问题对象
     */
    Question addQuestion(String words, Integer action) {
        Question question = Question.init();
        question.words(words).action(action);
        this.questionList.add(question);
        return question;
    }

    /**
     * 初始化重新做题的key
     */
    void initReplayKey() {
        this.questionList.forEach(question -> {
            question.initFunctionKey();
            question.getAnswerList().forEach(answer -> {
                if (answer.getBadEnding()) {
                    answer.setDeadWords(answer.getDeadWords()
                            .replace("{replayKey}", question.getReplayKey().toString())
                            .replace("{closeKey}", question.getCloseKey())
                    );
                }
            });
        });
    }

    /**
     * 根据action查询Question
     *
     * @param action action
     * @return Question
     */
    Question searchQuestionByAction(Integer action) {
        if (questionList == null) {
            return null;
        }
        return questionList.stream().filter(item -> item.getAction().equals(action)).findFirst().orElse(null);
    }


    /**
     * 根据关键词查询key
     *
     * @param key 关键词
     * @return Question
     */
    Question searchQuestionByKey(Integer key) {
        if (questionList == null) {
            return null;
        }
        return questionList.stream().filter(item -> item.getReplayKey().equals(key)).findFirst().orElse(null);
    }

    /**
     * 获取下一道题
     *
     * @param question 本题
     * @return 下一题
     */
    Question nextQuestion(Question question) {
        return this.questionList.stream().filter(item -> item.getReplayKey() > question.getReplayKey()).findFirst().orElse(null);
    }

    /**
     * 获取第一道题
     *
     * @return 题目
     */
    Question firstQuestion() {
        return this.questionList.stream().findFirst().orElse(null);
    }

}

/**
 * 问题内容
 */
class Question {
    @Getter
    private String words; // 问题题干
    @Getter
    private Integer action; // 对应的活动action
    @Getter
    private Integer replayKey; // 重新做这道题需要回复的数字
    @Getter
    private String closeKey; // 关闭key
    @Getter
    private List<Answer> answerList; // 选项列表


    public Question() {
        this.answerList = Lists.newArrayList();
    }

    public static Question init() {
        return new Question();
    }

    Question words(String words) {
        this.words = words;
        return this;
    }

    public Question action(Integer action) {
        this.action = action;
        return this;
    }

    /**
     * 初始化重做选择题的回复数字,以及结束题目的key
     * 规则：最后一个答案选项的数字+1
     */
    Question initFunctionKey() {
        if (this.answerList != null && !CollectionUtils.isEmpty(this.answerList)) {
            this.replayKey = this.answerList.get(this.answerList.size() - 1).getKey() + 1;
            this.closeKey = "00";
        }
        return this;
    }


    /**
     * 添加答案选项
     *
     * @param key       用户需要回复的数字
     * @param words     文本
     * @param deadWords 遗言
     */
    Question addAnswer(Integer key, String words, String deadWords, Integer sequence) {
        Answer answer = new Answer();
        answer.setKey(key);
        answer.setWords(words);
        answer.setBadEnding(deadWords != null);
        this.answerList.add(answer);
        answer.setDeadWords(deadWords);
        answer.setBadEndingSequence(sequence);
        return this;
    }

    /**
     * 通过key获取答案
     *
     * @param key 用户输入的关键字
     * @return 答案
     */
    Answer searchAnswerByKey(Integer key) {
        return this.answerList.stream().filter(item -> item.getKey().equals(key)).findFirst().orElse(null);
    }
}

/**
 * 答案选项
 */
@Data
class Answer {
    private Integer key; // 需要回复的数字
    private String words; // 内容
    private Boolean badEnding; // 是否badEnding
    private String deadWords; // 遗言,badEnding触发的回复
    private Integer badEndingSequence;
}