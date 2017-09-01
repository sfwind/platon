package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.LiveRedeemCodeDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
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
import com.iquanwai.platon.biz.util.PromotionConstants;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    public static final String CURRENT_GAME = PromotionConstants.Activities.CaitongLive;

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


    @PostConstruct
    public void initScript() {
        theatreScript = new TheatreScript();
        theatreScript.setPrologue("圈外博物馆是一所神秘的博物馆，据传闻里面蕴藏着无数知识瑰宝，只有聪明勇敢的人才能找到里面所隐藏的宝藏，造福人类。");
        theatreScript.setEndingWords("你念出了正确的咒语，霸王龙的骨架慢慢停止了前进，又恢复成了一个展览品。采铜馆长拍拍你的肩膀，指了指前面的宝箱，示意你过去拿。\n" +
                "\n" +
                "你跑到宝箱面前，颤抖地打开了箱子，发现里面竟然是一张价值264元的邀请证，你可以用这个邀请券免费让你的3个小伙伴听课。（邀请券已经累计放入你的背包里了，你可以回复背包查看你的邀请券）\n" +
                "\n" +
                "恭喜你，你已经完成了本次探险，如果之前没有领取到兑换码和邀请券的勇士可以回复【背包】查看你的兑换码和邀请券。通过你的邀请券进来的朋友享受“勇士の朋友”特殊待遇，可以免费听本次直播哦。\n");

        theatreScript.addQuestion(
                "你决定一探究竟，背着行囊来到了圈外博物馆。博物馆大门敞开着，里面黑洞洞的一片。\n" +
                        "在你打算直接跨入大门之时，两个守卫突然现身、拦住了你，声称你带了某件【圈外博物馆】的违禁品。\n" +
                        "你说：“我去，竟然不让带…… ”",
                CURRENT_ACTION.Question1)
                .addAnswer(11, "圈圈写的《请停止无效努力》一书", "守卫立马对小黄书鞠了一躬，毕竟小黄书是逆袭圣经啊！" +
                        "\n" +
                        "很抱歉，你连博物馆的门都没进去就失败了，你可以回复 {replayKey} 重新挑战")
                .addAnswer(12, "能上【圈外同学】课程的手机", "守卫掏出了自己的课程手机上起了【圈外同学】小课，说：“哥们，一起来！")
                .addAnswer(13, "能打各类游戏的小霸王游戏机", null);
        theatreScript.addQuestion(
                "你成功地舍弃了你最爱的小霸王学习机，为了鼓励你，我们决定免费送你一次圈外博物馆的直播分享。（兑换码已经放入你的背包里了，你可以随时回复背包查看兑换码）\n" +
                        "\n" +
                        "现在请你继续往下走，后面还有价值528元的宝藏等着你拿呢。\n" +
                        "\n" +
                        "刚进入了博物馆的大门，前面有一个老大爷，站在博物馆楼前，他看着你说：“找到圈外博物馆首任馆长，ta才能给你你想要的东西，快去吧年轻人，要相信你自己。”你推开门，前方是一个看不见尽头的走廊，走廊的左手边挂着一排人物画像。你要在这里找到首任馆长的画像，于是你走向了：\n",
                CURRENT_ACTION.Question2)
                .addAnswer(21, "孙圈圈的画像", "圈圈开始给你讲解结构化思维，一开口就再也没有停下\n" +
                        "\n" +
                        "你听着圈圈的声音昏昏欲睡，不过你可以回复{replayKey}摆脱圈圈的控制")
                .addAnswer(22, "马徐骏的画像", null)
                .addAnswer(23, "特朗普的画像", "特朗普睁开了眼睛，并骂了你一句：“I'll build a wall between us and let you pay for it！“\n" +
                        "\n" +
                        "你高声喊道：Hail Hydra！回复{replayKey}就可以重新挑战！"
                );
        theatreScript.addQuestion(
                "你对着马徐骏的画像念了一段咒语：“马馆长好帅！阿哩啊哩！”画像逐渐变亮。。。\n" +
                        "\n" +
                        "马馆长被你成功唤醒了，作为奖励，你得到了一个价值88元的免费邀请券，能够让你的一位小伙伴免费收听本次直播分享。（邀请券已经放入你的背包里了，你可以随时回复背包查看你的邀请券）\n" +
                        "\n" +
                        "\n" +
                        "马徐骏馆长缓缓睁开眼睛，张口来了一段freestyle，你一边听着他的freestyle，一边想着怎么找到下一步的线索。你应该怎么说：",
                CURRENT_ACTION.Question3)
                .addAnswer(31, "你的梦想是什么？", "马徐骏说完他的梦想之后闭上了眼睛，于是你还是不知道要怎么走出这个房间\n" +
                        "\n" +
                        "你迷失在这个走廊中，不过你可以回复{replayKey}再次挑战")
                .addAnswer(32, "马老师给我签个名！", "马徐骏告诉你藏有他签名照的柜子在哪里，但是你还是不知道怎么走出这个房间\n" +
                        "\n" +
                        "你迷失在这个走廊中，不过你可以回复{replayKey}再次挑战")
                .addAnswer(33, "沟通前利用5why分析法澄清本质问题：如何拿到知识瑰宝？", null);
        theatreScript.addQuestion(
                "马徐骏馆长完整的告诉了你前因后果，并帮你打开了通往下一个房间的大门。原来大门就藏在画像背后。\n" +
                        "\n" +
                        "得到马徐骏馆长的指点，你推开第一扇门，发现这里竟然放着泰坦尼克号的模型。当你从旁边经过的时候，Jack从水里冒了出来：年轻人，想要过去，先回答我一个问题，我已经在水下思考几百年了，你知道什么是冰山模型吗？\n",
                CURRENT_ACTION.Question4)
                .addAnswer(41, "我在圈外同学小课里看到过：冰山模型全面地描述了一个人所有的内在价值要素", null)
                .addAnswer(42, "冰山模型和泰坦尼克号的模型原本是一对", "原来你也不知道啊，那你下来陪我一起想吧（你被拉入了水中）\n" +
                        "\n" +
                        "你被Jack拖入水中，不过你可以回复{replayKey}再次挑战")
                .addAnswer(43, "不理他，直接往前走", "你发现越走越冷，回过神来发现自己竟然已经走到了水中。。。\n" +
                        "\n" +
                        "你被Jack拖入水中，不过你可以回复{replayKey}再次挑战");
        theatreScript.addQuestion(
                "啊！”原来学过圈外小课的人都知道“，Jack长叹一口气，慢慢地沉了下去。。。你继续往前走\n" +
                        "\n" +
                        "摆脱了Jack的你继续往前走，几分钟后发现前面有一扇长满青苔的石门。你好容易看清楚门上的字：大声说出本次博物馆奇妙夜的主题。\n",
                CURRENT_ACTION.Question5)
                .addAnswer(51, "用高效的沟通铺平你的职场之路", "愚昧无知的人类，这都不知道就想探寻我的宝藏，石门向你怒吼道。\n" +
                        "\n" +
                        "你怎么敲也敲不开石门，不过你可以回复{replayKey}再次挑战\n")
                .addAnswer(52, "啊～～～五环，你比四环多一环～～～", "愚昧无知的人类，这都不知道就想探寻我的宝藏，石门向你怒吼道。\n" +
                        "\n" +
                        "你怎么敲也敲不开石门，不过你可以回复{replayKey}再次挑战")
                .addAnswer(53, "职场精进与专家养成", null);
        theatreScript.addQuestion("石门上的字慢慢消退，你听到了石锁转动发出沉闷的声音。门开了，你走了进去。正当你走过石门的时候，石门缓缓地说道：你真是厉害，我再送你一张价值88元的免费听课券吧，这次你可以免费邀请1个小伙伴免费听课了。（邀请券已经累积算放到你的背包里了，你可以随时回复背包查看你的邀请券）\n" +
                        "\n" +
                        "你在前面看到了一位穿着斗篷的男子。你上前询问，得知这位男子就是采铜。这时，那男子问你：“你知道我的江湖名号吗？”",
                CURRENT_ACTION.Question6)
                .addAnswer(61, "挖矿的？难道你就是传说中的比特币大亨！", "这位男子听到答案哈哈大笑，你以为自己猜对了，却发现神秘男子已经消失不见\n" +
                        "\n" +
                        "没办法，谁叫你说错话了呢？回复{replayKey}再次挑战吧")
                .addAnswer(62, "知乎心理大v，畅销书《精进》作者", null)
                .addAnswer(63, "音乐大神，刚在网易云音乐发布了新专辑", "小伙子，很有想法嘛，和我学Freestyle吧。\n" +
                        "\n" +
                        "于是，你和采铜老师在Freestyle的路上越走越远，回复{replayKey}再次挑战吧");
        theatreScript.addQuestion(
                "这位神秘男子向你走来，嘴角露出了神秘的微笑\n" +
                        "\n" +
                        "你好不容易答对前一个问题，那个叫采铜的男子又问了你一个问题：“你知道为什么我们叫圈外博物馆吗？”你该怎么回答？",
                CURRENT_ACTION.Question7)
                .addAnswer(71, "你们收藏了很多历史文物", "采铜馆长对这个回答并不满意，长叹一声消失在走廊的尽头\n" +
                        "\n" +
                        "别说了，回复{replayKey}再答一次吧")
                .addAnswer(72, "你们希望通过12次大咖分享直播提升职场人的通用能力", null)
                .addAnswer(73, "因为博物馆一听就很有文化", "采铜馆长对这个回答并不满意，长叹一声消失在走廊的尽头\n" +
                        "\n" +
                        "别说了，回复{replayKey}再答一次吧");
        theatreScript.addQuestion(
                "嗯，看来你准备的很充分啊，跟我走吧，采铜向你挥挥手。\n" +
                        "\n" +
                        "你这才知道采铜就是圈外博物馆的本月馆长。这个时候，在阴暗角落冲出了一只怒吼中的霸王龙，在强大的敌人面前，你的战斗力太弱了，这时你想：",
                CURRENT_ACTION.Question8)
                .addAnswer(81, "果然不上圈外同学小课，战斗力跟不上啊", null)
                .addAnswer(82, "你后退一步，把采铜馆长推了出去", "霸王龙就像没看到采铜馆长一般，直向你奔来，一尾巴把你甩飞到墙上，抠都抠不下来\n" +
                        "\n" +
                        "回复{replayKey}可以召唤蓝翔挖掘机把你从墙上挖下来")
                .addAnswer(83, "你转头撒腿就跑，留给采铜馆长一个潇洒的背影", "你永远失去了得到珍宝的机会！\n" +
                        "\n" +
                        "回复{replayKey}再重来吧！不要怂，年轻人！");
        theatreScript.addQuestion("你意识到了自己因为没上圈外同学小课而战斗力不足，懊恼不已。\n" +
                        "\n" +
                        "采铜馆长好像看穿了你的想法并用意念对你说：“去那边3本书里挑一本我编写的秘法宝典，这个可以大幅度增加你的战斗力。”你听完以后火速地跑过去，选出了这本书：",
                CURRENT_ACTION.Question9)
                .addAnswer(91, "《请停止无效努力》", "”这不就是我交给门卫的小黄书嘛！“你津津有味地读了起来，忘记了自己要做什么。\n" +
                        "\n" +
                        "你被霸王龙吃掉了。。回复{replayKey}复活吧")
                .addAnswer(92, "《精进：如何成为一个厉害的人》", null)
                .addAnswer(93, "《毛泽东思想和中国特色社会主义理论体系概论》", "这个时候拿到了这本书，你的心里仿佛有几百万只草泥马呼啸而过。。\n" +
                        "\n" +
                        "你被自己心里的草泥马踩死了。。回复{replayKey}复活吧");
        theatreScript.addQuestion("拿起书的一瞬间，封面上的乌鸦通体发亮，吓得霸王龙倒退了几步。你知道自己选对了！\n" +
                        "\n" +
                        "你翻开书，发现里面一共有三条咒语，你选对了以后真的战斗力提升了一大截。\n",
                CURRENT_ACTION.Question10)
                .addAnswer(101, "用持续精确的努力，撬动更大的可能，这便是精进", null)
                .addAnswer(102, "稳住，我们能赢！", "你找错了咒语，你被霸王龙一脚踢出了博物馆，你口中大喊：我还会回来的。。\n" +
                        "\n" +
                        "回复{replayKey}再次回来吧！")
                .addAnswer(103, "发现本质问题，减少无效努力", "这个咒语看上去很正确，但是只对霸王龙造成了一点伤害，你被霸王龙一口吃了下去。\n" +
                        "\n" +
                        "回复{replayKey}再次回来吧！");

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
            Question maxPlayQustion = this.loadMaxPlayQuestion(profile.getId());
            customerMessageService.sendCustomerMessage(profile.getOpenid(), "你的兑换码是:" + liveRedeemCode.getCode(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            if (maxPlayQustion != null && maxPlayQustion.getAction() > CURRENT_ACTION.Question3) {
                // 做到第四题才会有邀请券,发海报
                String mediaId = generateSharePage(profile);
                customerMessageService.sendCustomerMessage(profile.getOpenid(), mediaId, Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
            }
        }
    }

    /**
     * 完成到最后的题目
     *
     * @param profileId 用户id
     * @return 题目
     */
    private Question loadMaxPlayQuestion(Integer profileId) {
        PromotionActivity activity = promotionActivityDao.loadMaxPlayQustion(profileId, CURRENT_GAME, CURRENT_ACTION.Backpack);
        if (activity == null) {
            return null;
        }
        return theatreScript.searchQuestionByAction(activity.getAction());
    }

    /**
     * 处理重玩逻辑
     *
     * @param profile  用户信息
     * @param question 当前在做的question
     * @param key      输入的key
     */
    private void handleReplay(Profile profile, Question question, Integer key) {
        // 输入的应该是Question
        Question wannaQuestion = theatreScript.searchQuestionByKey(key);
        if (wannaQuestion == null) {
            // TODO error
            customerMessageService.sendCustomerMessage(profile.getOpenid(), "输入的不是想要重做的题目序号", Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        } else {
            Question lastQuestion = this.loadMaxPlayQuestion(profile.getId());
            // 检查有没有做到这一题
            if (lastQuestion == null) {
                // 之前没有做过题,推送第一道题目
                Question firstQuestion = theatreScript.firstQuestion();
                this.sendQuestionToUser(profile, firstQuestion);
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
            customerMessageService.sendCustomerMessage(profile.getOpenid(), answer.getDeadWords(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        } else {
            // go live 回答正确
            if (theatreScript.isLastQuestion(question)) {
                // 最后一道题回答正确，送奖品
                PromotionActivity completeAction = new PromotionActivity();
                completeAction.setAction(CURRENT_ACTION.Complete);
                completeAction.setActivity(CURRENT_GAME);
                completeAction.setProfileId(profile.getId());
                promotionActivityDao.insertPromotionActivity(completeAction);
                // 送出礼物，延后到发背包的时候
                customerMessageService.sendCustomerMessage(profile.getOpenid(), theatreScript.getEndingWords(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
            } else {
                // 不是最后一题，推送下一题
                Question nextQuestion = theatreScript.nextQuestion(question);
                if (nextQuestion == null) {
                    // TODO ERROR
                    logger.error("异常，没有下一题了,{}", question);
                } else {
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
        StringBuilder message = new StringBuilder(question.getWords());
        List<Answer> answers = question.getAnswerList();
        answers.forEach(answer -> {
            message.append("\n").
                    append(answer.getKey()).append(" : ").append(answer.getWords());
        });
        message.append("\n\n")
                .append("请回复题目编号进行闯关\n");
        customerMessageService.sendCustomerMessage(profile.getOpenid(), message.toString(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }


    @Override
    public void handleTheatreMessage(WechatMessage wechatMessage) {
        logger.info("handleTheatreMessage");
        String message = wechatMessage.getMessage();
        Profile profile = accountService.getProfile(wechatMessage.getOpenid());
        // 查询该用户正在做的题
        PromotionActivity activity = promotionActivityDao.loadRecentPromotionActivity(profile.getId(), CURRENT_GAME, CURRENT_ACTION.Backpack);
        // 用户正在进行的操作
        Integer currentAction = activity.getAction();
        Question question = theatreScript.searchQuestionByAction(currentAction);
        if (message.equals(BACKPACK)) {
            // 处理背包逻辑
            this.handleBackpackMessage(profile);
        } else if (StringUtils.isNumeric(message)) {
            Integer key = Integer.parseInt(message);
            Answer answer = question != null ? question.searchAnswerByKey(key) : null;
            if (answer == null) {
                // 处理重玩逻辑
                this.handleReplay(profile, question, key);
            } else {
                // 处理答题逻辑
                this.handleAnswer(profile, question, answer);
            }
        }
        // 输入的不是背包，也不是数字，ignore
    }


    @Override
    public Boolean isPlayingTheatre(WechatMessage wechatMessage) {
        Profile profile = accountService.getProfile(wechatMessage.getOpenid());
        PromotionLevel liveLevel = promotionLevelDao.loadByProfileId(profile.getId(), CURRENT_GAME);
        return liveLevel != null;
    }

    @Override
    public void startGame(Profile profile) {
        // 由mq发起，那里会插入level，所以不检查了
        PromotionActivity startAction = new PromotionActivity();
        startAction.setAction(CURRENT_ACTION.Question1);
        startAction.setActivity(CURRENT_GAME);
        startAction.setProfileId(profile.getId());
        promotionActivityDao.insertPromotionActivity(startAction);
        // 发送开场白以及第一题文案:
        customerMessageService.sendCustomerMessage(profile.getOpenid(), theatreScript.getPrologue(), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        // 发送第一题文案
        Question firstQuestion = theatreScript.firstQuestion();
        this.sendQuestionToUser(profile, firstQuestion);
    }

    private String generateSharePage(Profile profile) {
        String scene = CURRENT_GAME + "_" + profile.getId();
        BufferedImage qrBuffer = qrCodeService.loadQrImage(scene);
        return uploadResourceService.uploadResource(qrBuffer);
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
            question.initReplayKey();
            question.getAnswerList().forEach(answer -> {
                if (answer.getBadEnding()) {
                    answer.setDeadWords(answer.getDeadWords().replace("{replayKey}", question.getReplayKey().toString()));
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
     * 初始化重做选择题的回复数字
     * 规则：最后一个答案选项的数字+1
     */
    Question initReplayKey() {
        if (this.answerList != null && !CollectionUtils.isEmpty(this.answerList)) {
            this.replayKey = this.answerList.get(this.answerList.size() - 1).getKey() + 1;
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
    Question addAnswer(Integer key, String words, String deadWords) {
        Answer answer = new Answer();
        answer.setKey(key);
        answer.setWords(words);
        answer.setBadEnding(deadWords != null);
        this.answerList.add(answer);
        answer.setDeadWords(deadWords);
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
}