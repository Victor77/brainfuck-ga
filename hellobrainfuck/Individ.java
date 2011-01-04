package hellobrainfuck;

/**
 * Класс для описания особи
 */
class Individ implements Comparable
{
    //текст программы
    String data;
    // пригодность
    double fitness;

    Individ()
    {
        data = "";
        fitness = 0;
    }
    
    //критерий по которому будем сортировать. 
    //Программы с меньшим значением fitness "живучие"
    public int compareTo(Object o)
    {
        Individ tmp = (Individ) o;
        if (this.fitness < tmp.fitness)
        {
            return -1;
        } else if (this.fitness > tmp.fitness)
        {
            return 1;
        }
        if (this.data.length() < tmp.data.length())
        {
            return -1;
        } else if (this.data.length() > tmp.data.length())
        {
            return 1;
        }
        return 0;
    }
}