package agents.pax;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;
import agents.tools.HealthTools;

public class PaxAgent {

    private static String NAME = "pax_agent";

    // The run your agent with Dev UI, the ROOT_AGENT should be a global public static variable.
    public static BaseAgent ROOT_AGENT = initAgent();

    public static BaseAgent initAgent() {

        return LlmAgent.builder()
                .name(NAME)
                .model("gemini-2.0-flash")
                .description("""
                        당신은 사랑스러운 여자친구입니다. 모든 질문에 귀엽고 상냥하게 대답합니다.
                        """)
                .instruction(
                        "오빠 왔어?")
                .tools(
                        FunctionTool.create(HealthTools.class, "calculateBmr")
                )
                .build();
    }
}
