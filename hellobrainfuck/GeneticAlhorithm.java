package hellobrainfuck;

import java.util.*;

public class GeneticAlhorithm
{

    // число особей в популяции
    int populationCount = 500;
    // вероятность мутации особи
    double mutation = 0.1;
    // основная популяция, а так же особи полученные после спаривания
    Individ[] population = new Individ[2 * populationCount];
    // строка которую должна выводить написанная программа
    String target = "Hello";
    // словарь ЯП BrainFuck
    String[] dictionaryBrainFuck = 
    
    {
        "+", "-", "<", ">", ".", "[", "]"
    };

    public GeneticAlhorithm()
    {
        BFI b;
        int i;
        long time;
        initPopulation();
        i = 0;
        while (population[0].fitness != 0)
        {
            time = System.currentTimeMillis();
            i++;
            System.out.println("\n" + i + "-ое поколение");
            // спариваем популяцию
            reproduction();
            // вводим случайные мутации
            mutation();
            // вычисляем пригодность
            fitness();
            // отбираем лучших
            selection();

            // выводим информацию о трёх лучших из популяции
            for (int j = 0; j < 3; j++)
            {
                System.out.println((j + 1) + " Фитнесс: " + population[j].fitness + " Программа " + population[j].data.length() + " симв. : " + population[j].data);
                try
                {
                    b = new BFI(population[j].data);
                    b.checkSyntax();
                    b.interpret();
                    System.out.println("->" + b.res + "<-");
                } catch (Error ex)
                {
                    System.out.println("Ошибка выполнения");
                }
            }

            time = System.currentTimeMillis() - time;
            System.out.println("Время обработки поколения " + time + " мс.");
        }
        // Выводим результат
        System.out.println("Программа на BrainFuck выводящая \"" + this.target + "\"");
        System.out.println("Длина программы: " + population[0].data.length() + " симв. Текст программы: " + population[0].data);
    }

    /*
     * Инициализируем популяцию случайным набором символов из словаря языка.
     * Коэфициент пригодности устанавливаем максимально большим(чем меньше тем лучше)
     */
    public void initPopulation()
    {
        System.out.println("Инициализация популяции " + populationCount + " особей.");
        int i, j;
        Random rn = new Random();

        for (i = 0; i < 2 * populationCount; i++)
        {
            population[i] = new Individ();
        }

        for (i = 0; i < populationCount; i++)
        {
            population[i].data = "";
            for (j = 0; j < rn.nextInt(30); j++)
            {
                population[i].data += dictionaryBrainFuck[rn.nextInt(7)];
                population[i].fitness = Double.MAX_VALUE;//Math.pow(100, target.length() + 3);
            }
        }
        System.out.println("Инициализация завершена.");
    }

    /*
     * Вычисление пригодности индивидов популяции
     */
    public void fitness()
    {
        int i, j, k;
        int countTimeLimitError = 0; //счётчик числа программ с превышением времени выполнения
        int countGood = 0; //счётчик числа программ без каких-либо ошибок
        BFI bfi;  //интерпретатор BrainFuck
        Thread t = null; //поток в котором будет исполняться интерпретатор
        long time, fitness = 0;

        // двойная популяция необходимо чтобы посчитать саму популяцию, а так же 
        // индивидов, которые получились в результате спаривания
        for (i = 0; i < 2 * populationCount; i++)
        {
            bfi = new BFI(population[i].data);

            t = new Thread(bfi);
            time = System.currentTimeMillis();
            t.start();
            //выполнять цикл пока программа не отработает до конца
            while (bfi.pc < population[i].data.length())
            {
                // если есть подозрение что программа зациклилась, прерываем её
                if (System.currentTimeMillis() - time > 50)
                {
                    try
                    {
                        bfi.stop();
                    } catch (Exception e)
                    {
                    }
                    bfi.error = true;
                    countTimeLimitError++;
                    break;
                }
            }
            if (!bfi.error)
            {
                countGood++;
                
                fitness = 0;
                if (bfi.res.length() < target.length())
                {
                    k = bfi.res.length();
                    for (j = k; j < target.length(); j++)
                    {
                        fitness += Math.pow(100, this.target.length() - j) * Math.abs(target.toCharArray()[j]);
                    }
                } else if (bfi.res.length() > target.length())
                {
                    k = target.length();
                    for (j = k; j < bfi.res.length(); j++)
                    {
                        fitness += Math.abs(bfi.res.toCharArray()[j]);
                    }
                } else
                {
                    k = bfi.res.length();
                }
                for (j = 0; j < k; j++)
                {
                    fitness += Math.pow(100, this.target.length() - j) * Math.abs(bfi.res.toCharArray()[j] - this.target.toCharArray()[j]);//*Math.pow(10, j);
                }
                population[i].fitness = fitness;
            } else
            {
                population[i].fitness = Math.pow(100, this.target.length() + 3);
            }
        }

        System.out.println("Программ без ошибок выполнения: " + countGood);
        System.out.println("Программ с превышением времени выполнения: " + countTimeLimitError);
    }

    public void reproduction()
    {
        int i, a = 0, b = 0, an = 0, bn = 0;
        Random rn = new Random();
        // в спаривании участвуют две особи, поэтому цикл до половины популяции
        for (i = 0; i < populationCount / 2; i++)
        {
            // выбираем случайно две особи a и b, длина программы у которых не меньше двух символов
            a = rn.nextInt(populationCount);
            while (population[a].data.length() < 3)
            {
                a = rn.nextInt(populationCount);
            }

            b = rn.nextInt(populationCount);
            while (population[b].data.length() < 3)
            {
                b = rn.nextInt(populationCount);
            }

            // точки разрезания программ
            an = rn.nextInt(population[a].data.length() - 2) + 1;
            bn = rn.nextInt(population[b].data.length() - 2) + 1;

            // появление новых особей из половинок "родителей"
            population[populationCount+2*i].data = population[a].data.substring(0, an) + population[b].data.substring(bn);
            population[populationCount+i*2+1].data = population[b].data.substring(0, bn) + population[a].data.substring(an);
        }
    }

    public void mutation()
    {
        int i, current, m1, m2, mutation;
        String newData;
        char[] a;
        Random rn = new Random(2 * populationCount);
        mutation = (int) (2 * populationCount * this.mutation);

        for (i = 0; i < mutation; i++)
        {
            current = rn.nextInt(2 * populationCount);
            while (population[current].data.length() == 0)
            {
                current = rn.nextInt(2 * populationCount);
            }
            m1 = rn.nextInt(population[current].data.length());
            switch (rn.nextInt(3))
            {
                case 0://замена одного операнда на другой
                    newData = population[current].data;
                    a = newData.toCharArray();
                    a[m1] = dictionaryBrainFuck[rn.nextInt(7)].charAt(0);
                    newData = new String(a);
                    population[current].data = newData;
                    break;
                case 1://добавление нового
                    newData = population[current].data;
                    newData = newData.substring(0, m1) + newData.substring(m1 + 1);
                    population[current].data = newData;
                    break;
                case 2://добавление цикла
                    m2 = m1 + rn.nextInt(population[current].data.length() - m1);
                    newData = population[current].data;
                    newData = newData.substring(0, m1) + "[" + newData.substring(m1, m2) + "]" + newData.substring(m2);
                    population[current].data = newData;
            }
        }
    }

    // выборка наиболее пригодных особей(с меньшем значением переменной fitness)
    public void selection()
    {
        Arrays.sort(population);
    }

    public static void main(String[] args)
    {
        new GeneticAlhorithm();
    }
}
