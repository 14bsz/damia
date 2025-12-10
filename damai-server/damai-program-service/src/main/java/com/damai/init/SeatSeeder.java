package com.damai.init;

import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.damai.entity.Seat;
import com.damai.entity.TicketCategory;
import com.damai.enums.SellStatus;
import com.damai.mapper.SeatMapper;
import com.damai.mapper.TicketCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "damai.seat.seeder", name = "enabled", havingValue = "true")
public class SeatSeeder implements CommandLineRunner {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private TicketCategoryMapper ticketCategoryMapper;

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private com.damai.service.SeatService seatService;

    @Value("${damai.seat.seeder.program-id:2}")
    private Long programId;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting SeatSeeder for programId: " + programId);

        // 如果已有座位则跳过，避免重复删除和插入
        Long existCount = seatMapper.selectCount(Wrappers.lambdaQuery(Seat.class).eq(Seat::getProgramId, programId));
        if (existCount != null && existCount > 0) {
            System.out.println("SeatSeeder skipped. Existing seats found: " + existCount);
            return;
        }

        // 2. Get ticket categories
        List<TicketCategory> categories = ticketCategoryMapper
                .selectList(Wrappers.lambdaQuery(TicketCategory.class).eq(TicketCategory::getProgramId, programId));

        if (categories.isEmpty()) {
            System.out.println("No ticket categories found for program " + programId);
            return;
        }

        // Sort by price descending (usually higher price = front rows)
        categories.sort(Comparator.comparing(TicketCategory::getPrice).reversed());

        // 3. 生成 500 个座位 (20 行 x 25 列)，并使用批量插入
        int totalRows = 20;
        int cols = 25;
        int currentRow = 1;
        java.util.ArrayList<Seat> batchSeats = new java.util.ArrayList<>(totalRows * cols);

        // Distribute rows among categories
        int rowsPerCat = totalRows / categories.size();
        int remainder = totalRows % categories.size();

        for (int i = 0; i < categories.size(); i++) {
            TicketCategory cat = categories.get(i);
            int rowsForThisCat = rowsPerCat + (i < remainder ? 1 : 0);

            for (int r = 0; r < rowsForThisCat; r++) {
                for (int c = 1; c <= cols; c++) {
                    Seat seat = new Seat();
                    seat.setId(uidGenerator.getUid());
                    seat.setProgramId(programId);
                    seat.setTicketCategoryId(cat.getId());
                    seat.setRowCode(currentRow);
                    seat.setColCode(c);
                    seat.setSeatType(1);
                    seat.setPrice(cat.getPrice());
                    seat.setSellStatus(SellStatus.NO_SOLD.getCode());
                    batchSeats.add(seat);
                }
                currentRow++;
            }
        }

        // 使用 MyBatis-Plus saveBatch 分批插入
        boolean ok = seatService.saveBatch(batchSeats, 500);
        System.out.println("SeatSeeder completed. Added " + (ok ? batchSeats.size() : 0) + " seats.");
    }
}
