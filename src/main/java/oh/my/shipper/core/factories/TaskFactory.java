package oh.my.shipper.core.factories;

import lombok.Data;
import oh.my.shipper.core.api.Input;
import oh.my.shipper.core.api.Recyclable;
import oh.my.shipper.core.api.Scheduled;
import oh.my.shipper.core.task.StandardLoopShipperTask;
import oh.my.shipper.core.task.StandardScheduleShipperTask;
import oh.my.shipper.core.task.StandardSimpleShipperTask;

import java.util.ArrayList;
import java.util.List;

/**
 * task工厂类
 * 这个类不允许重写
 * 若需要添加新的task story,必须通过设置stories的方式
 * 默认为story
 */
@Data
public final class TaskFactory {
    public static final List<TaskImplStory> DEFAULT_STORIES = new ArrayList<>(3);
    public static final TaskImplStory DEFAULT_STORY = new TaskImplStory(Input.class, "simple", StandardSimpleShipperTask.class);

    static {
        DEFAULT_STORIES.add(new TaskImplStory(Recyclable.class, "loop", StandardLoopShipperTask.class));
        DEFAULT_STORIES.add(new TaskImplStory(Scheduled.class, "cron", StandardScheduleShipperTask.class));
    }

    /**
     * story列表
     */
    private List<TaskImplStory> stories = DEFAULT_STORIES;
    /**
     * 兜底的story
     */
    private TaskImplStory story = DEFAULT_STORY;

    /**
     * 按照shipper的引导口映射查找对应的实现
     * 按顺序
     * 默认情况下将会返回 story
     * @param clazz 当前的shipper类型
     * @return 一个task的story
     */
   public TaskImplStory findStory(Class<?> clazz) {
        for (TaskImplStory story : stories) {
            if (story.typeClazz.isAssignableFrom(clazz))
                return story;
        }
        return story;
    }

}
