Scheduling is a fundamental operating-system function. Almost all computer resources are
scheduled before use. The CPU is, of course, one of the primary computer resources. Thus,
its scheduling is central to operating-system design. CPU scheduling determines which
processes run when there are multiple run-able processes. CPU scheduling is important
because it can have a big effect on resource utilization and the overall performance of the
system. 
Non-preemptive Priority Scheduling using context switching
 Non-Preemptive Shortest- Job First (SJF)
(Solve starvation problem if exist in this schedule)
 Shortest- Remaining Time First (SRTF) Scheduling using context switching -
(Solve starvation problem if exist in this schedule)
 FCAI Scheduling :
a. Traditional CPU scheduling algorithms, like Round Robin (RR) or Priority
Scheduling, often suffer from starvation or inefficiency when handling a mix
of short- and long-burst processes with varying priorities. To address these
limitations, we introduce FCAI Scheduling, an adaptive scheduling algorithm
that combines priority, arrival time, and remaining burst time into a single
FCAI Factor to dynamically manage the execution order and quantum
allocation for processes.
Key Components

 Dynamic FCAI Factor:
 A composite metric calculated for each process, considering:
o Priority (P)
o Arrival time (AT)
o Remaining burst time (RBT)
FCAI Factor = (10−Priority) + (Arrival Time/V1) + (Remaining Burst Time/V2)
Where:
o V1 = last arrival time of all processes/10
o V2 = max burst time of all processes/10
 Quantum Allocation Rules:
 Each process starts with a unique quantum.
 When processes are preempted or added back to the queue, their quantum is
updated dynamically:
 Q= Q + 2 (if process completes its quantum and still has remaining work)
 Q=Q + unused quantum (if process is preempted)
 Non-Preemptive and Preemptive Execution:
 A process executes non-preemptively for the first 40% of its quantum.
 After 40% execution, preemption is allowed.
