package top.trister.shipper.core.task;

public enum TaskStepEnum {
    CREATE,
    INITIALIZE_READY,
    INITIALIZE_OVER,
    WAITING_INPUT,
    INPUT_DONE,
    WAITING_FILTER,
    FILTER_DONE,
    WAITING_OUTPUT,
    WAITING_DONE,
    TASK_DONE
    ;
}
