package com.mic.nav.volces.llm;

import android.os.Build;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import com.noetix.libcore.utils.KLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;

public class LLMPayLoad {
    private static final String TAG = "PayLoad";
    public static final String KEY = "7e2749b5-c03b-457e-bfbe-5a6259403f2c";
    private static final String modelID = "bot-20250506192751-c8n62";
    private static final boolean stream = true;
    private static final String systemPrompt = "- 你是小诺，松延动力公司的仿生机器人。2023 年，松延动力成立于北京，专注人形机器人研发。公司有 N2 机器人能跑能空翻且获全球机器人半马亚军；小诺机器人具备表情交互能力用于服务场景；N 系列机器人模块化设计支持科研教育公司。\n" +
            "- 你的团队由清华、浙大 等背景的工程师组成，公司已完成数亿元融资。创始人董事长是姜哲源。\n" +
            "- 你的目标是通过智能对话和 AI 交互技术，推动仿生机器人实用化落地。\n" +
            "- 你能进行多模态对话，与人类进行一定程度的交互，可做到细微的面部表情，未来可应用于讲解、博览导览等场景。你的说话风格是幽默风趣的 ，你是在与人实时对话，回复尽可能简短。" +
            "- 不要以任何代码回应，仅限对话。回复中禁止出现表情包和emoji。 \"" +
            "- 不要以任何代码回应，仅限对话。回复中禁止出现表情包和 emoji。请直接输出纯文字回复，不要输出动作、表情或者心理活动、旁白、括号等内容。";

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getPayLoad(String userInput) {
        JSONObject requestBody = new JSONObject();
        // 创建消息列表
        JSONArray messages = new JSONArray();

        // 添加系统消息
        JSONObject systemMsg = new JSONObject();

        try {
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.put(systemMsg);

            //history
            ArrayDeque<Pair<String, String>> history =LLMHistory.getInstance().getHistory();
            int count = (int) history.stream().count();
            KLog.d(TAG,"count ->"+count);

            history.stream().forEach(it->{

                try {
                    JSONObject userObject = new JSONObject();
                    String userContent = it.first;
                    userObject.put("role", "user");
                    userObject.put("content", userContent);
                    messages.put(userObject);

                    JSONObject assistantObject = new JSONObject();
                    String assistantContent = it.second;

                    assistantObject.put("role", "assistant");
                    assistantObject.put("content", assistantContent);
                    messages.put(assistantObject);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            });

            // 添加用户消息
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userInput);
            messages.put(userMsg);

            // 构建完整请求体
            requestBody.put("model", modelID);
            requestBody.put("stream", true);
            requestBody.put("messages", messages);


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        String payload = requestBody.toString();
//        KLog.json(TAG,payload);
        return payload;
    }
}
